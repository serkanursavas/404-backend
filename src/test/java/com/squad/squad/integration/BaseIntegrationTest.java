package com.squad.squad.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.*;

/**
 * Tüm integration testlerinin base class'ı.
 *
 * - Local PostgreSQL kullanılır (squad_test veritabanı)
 * - Flyway, test DB'sine migration'ları uygular
 * - Her test @BeforeEach ile tüm tablolar truncate edilir
 * - HTTP işlemleri için TestRestTemplate
 * - DB doğrulama için JdbcTemplate
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    /**
     * Test datasource → local squad_test DB
     * Credentials surefire plugin'de tanımlanmış (pom.xml)
     */
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:postgresql://localhost:5432/squad_test");
        registry.add("spring.datasource.username", () -> "squad_dev");
        registry.add("spring.datasource.password", () -> "squad_dev_2024");
    }

    @LocalServerPort
    int port;

    @Autowired
    protected JdbcTemplate jdbc;

    protected TestRestTemplate http = new TestRestTemplate();

    // ─── DB Reset ─────────────────────────────────────────────────────────────────
    // Her test başlamadan önce application data temizlenir.
    // persona ve game_location reference data olduğundan temizlenmez.
    @BeforeEach
    void resetDatabase() {
        jdbc.execute("""
                TRUNCATE rating, roster_persona, player_persona, goal, roster, game,
                         join_request, squad_request, group_membership, player, squad, "user"
                RESTART IDENTITY CASCADE
                """);
        // Reference data: MVP persona (test DB'de yoksa ekle)
        jdbc.execute("INSERT INTO persona (id, name, active) OVERRIDING SYSTEM VALUE VALUES (68, 'MVP', true) ON CONFLICT DO NOTHING");
    }

    // ─── URL Builder ─────────────────────────────────────────────────────────────
    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ─── HTTP Helper: Authenticated GET ──────────────────────────────────────────
    protected <T> ResponseEntity<T> get(String path, String token, Class<T> responseType) {
        return http.exchange(url(path), HttpMethod.GET, new HttpEntity<>(authHeaders(token)), responseType);
    }

    protected <T> ResponseEntity<T> getWithSquad(String path, String token, int squadId, Class<T> responseType) {
        return http.exchange(url(path), HttpMethod.GET, new HttpEntity<>(squadHeaders(token, squadId)), responseType);
    }

    // ─── HTTP Helper: Authenticated POST ─────────────────────────────────────────
    protected <T> ResponseEntity<T> post(String path, Object body, String token, Class<T> responseType) {
        return http.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, authHeaders(token)), responseType);
    }

    protected <T> ResponseEntity<T> postWithSquad(String path, Object body, String token, int squadId, Class<T> responseType) {
        return http.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, squadHeaders(token, squadId)), responseType);
    }

    // ─── HTTP Helper: Authenticated PUT ──────────────────────────────────────────
    protected <T> ResponseEntity<T> put(String path, Object body, String token, Class<T> responseType) {
        return http.exchange(url(path), HttpMethod.PUT, new HttpEntity<>(body, authHeaders(token)), responseType);
    }

    protected <T> ResponseEntity<T> putWithSquad(String path, Object body, String token, int squadId, Class<T> responseType) {
        return http.exchange(url(path), HttpMethod.PUT, new HttpEntity<>(body, squadHeaders(token, squadId)), responseType);
    }

    // ─── HTTP Helper: Authenticated DELETE ───────────────────────────────────────
    protected <T> ResponseEntity<T> delete(String path, String token, Class<T> responseType) {
        return http.exchange(url(path), HttpMethod.DELETE, new HttpEntity<>(authHeaders(token)), responseType);
    }

    protected <T> ResponseEntity<T> deleteWithSquad(String path, String token, int squadId, Class<T> responseType) {
        return http.exchange(url(path), HttpMethod.DELETE, new HttpEntity<>(squadHeaders(token, squadId)), responseType);
    }

    // ─── Headers ─────────────────────────────────────────────────────────────────
    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpHeaders squadHeaders(String token, int squadId) {
        HttpHeaders headers = authHeaders(token);
        headers.set("X-Group-Id", String.valueOf(squadId));
        return headers;
    }

    // ─── Helper: Kullanıcı oluştur ────────────────────────────────────────────────
    protected UserSession registerUser(String username) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", "Password1!");
        body.put("passwordAgain", "Password1!");

        ResponseEntity<Map> res = http.postForEntity(url("/api/users/createUser"), body, Map.class);
        if (res.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Failed to create user [" + username + "]: " + res.getBody());
        }
        String token = (String) res.getBody().get("token");
        return new UserSession(username, token);
    }

    // ─── Helper: SuperAdmin oluştur ───────────────────────────────────────────────
    // SuperAdmin flag'i DB'de set edilir. JWT değişmez; role kontrolleri DB'den yapılır.
    protected UserSession registerSuperAdmin(String username) {
        UserSession session = registerUser(username);
        jdbc.update("UPDATE \"user\" SET is_super_admin = true WHERE username = ?", username);
        return session;
    }

    // ─── Helper: Login ───────────────────────────────────────────────────────────
    protected String login(String username) {
        Map<String, Object> body = Map.of("username", username, "password", "Password1!");
        ResponseEntity<Map> res = http.postForEntity(url("/api/users/login"), body, Map.class);
        return (String) res.getBody().get("token");
    }

    // ─── Helper: Squad kurulum (tam flow) ────────────────────────────────────────
    @SuppressWarnings("unchecked")
    protected SquadSetup setupSquad(String adminUsername, UserSession superAdmin) {
        // 1. Admin kullanıcı oluştur
        UserSession admin = registerUser(adminUsername);

        // 2. Squad oluşturma talebi
        Map<String, Object> createReq = new HashMap<>();
        createReq.put("squadName", adminUsername + "'s Squad");
        createReq.put("playerName", "Ali");
        createReq.put("playerSurname", "Veli");
        createReq.put("playerPosition", "FW");
        createReq.put("playerFoot", "RIGHT");
        post("/api/squads/request-create", createReq, admin.token(), String.class);

        // 3. SuperAdmin talebi listele → request ID'yi al
        ResponseEntity<List> pendingRes = get("/api/squads/super/pending-requests", superAdmin.token(), List.class);
        List<Map<String, Object>> pending = (List<Map<String, Object>>) pendingRes.getBody();
        // En son eklenen talep (bu admin'in talebi)
        Map<String, Object> myRequest = pending.stream()
                .filter(r -> adminUsername.equals(r.get("requestedByUsername")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Squad request not found for " + adminUsername));
        Integer requestId = (Integer) myRequest.get("id");

        // 4. Onayla
        put("/api/squads/super/approve/" + requestId, null, superAdmin.token(), String.class);

        // 5. Admin login → güncel squad bilgisi
        String freshToken = login(adminUsername);
        ResponseEntity<List> squadsRes = get("/api/squads/my-squads", freshToken, List.class);
        Map<String, Object> squadSummary = (Map<String, Object>) ((List<?>) squadsRes.getBody()).get(0);
        int squadId = (Integer) squadSummary.get("id");

        // 6. inviteCode DB'den
        String inviteCode = jdbc.queryForObject("SELECT invite_code FROM squad WHERE id = ?", String.class, squadId);

        // 7. Admin player ID
        Integer adminPlayerId = jdbc.queryForObject(
                "SELECT p.id FROM player p " +
                "JOIN group_membership gm ON gm.player_id = p.id " +
                "JOIN \"user\" u ON u.id = gm.user_id " +
                "WHERE gm.squad_id = ? AND u.username = ?",
                Integer.class, squadId, adminUsername);

        int locationId = createGameLocation("Test Saha", squadId);

        return new SquadSetup(new UserSession(adminUsername, freshToken), squadId, inviteCode, adminPlayerId, locationId);
    }

    // ─── Helper: Kullanıcıyı squad'a ekle (join flow) ────────────────────────────
    @SuppressWarnings("unchecked")
    protected MemberSetup joinSquad(String username, SquadSetup squad, UserSession squadAdmin) {
        UserSession member = registerUser(username);

        // Join talebi gönder
        Map<String, Object> joinReq = new HashMap<>();
        joinReq.put("inviteCode", squad.inviteCode());
        joinReq.put("playerName", username);
        joinReq.put("playerSurname", "Test");
        joinReq.put("playerPosition", "MF");
        joinReq.put("playerFoot", "LEFT");
        post("/api/squads/request-join", joinReq, member.token(), String.class);

        // Admin join taleplerini listele → requestId
        ResponseEntity<List> pendingRes = getWithSquad(
                "/api/squads/admin/join-requests", squadAdmin.token(), squad.squadId(), List.class);
        Map<String, Object> joinReqData = ((List<Map<String, Object>>) pendingRes.getBody())
                .stream()
                .filter(r -> username.equals(r.get("username")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Join request not found for " + username));
        Integer joinRequestId = (Integer) joinReqData.get("id");

        // Admin onayla
        putWithSquad("/api/squads/admin/approve-join/" + joinRequestId, null,
                squadAdmin.token(), squad.squadId(), String.class);

        // Güncel token + player ID
        String freshToken = login(username);
        Integer playerId = jdbc.queryForObject(
                "SELECT p.id FROM player p " +
                "JOIN group_membership gm ON gm.player_id = p.id " +
                "JOIN \"user\" u ON u.id = gm.user_id " +
                "WHERE gm.squad_id = ? AND u.username = ?",
                Integer.class, squad.squadId(), username);

        return new MemberSetup(new UserSession(username, freshToken), playerId);
    }

    // ─── Helper: GameLocation oluştur ────────────────────────────────────────────
    protected int createGameLocation(String name, int squadId) {
        return jdbc.queryForObject(
                "INSERT INTO game_location (location, active, squad_id) VALUES (?, true, ?) RETURNING id",
                Integer.class, name, squadId);
    }

    // ─── Helper: Maç oluştur (gelecek tarih ile oluştur, sonra geçmişe çek) ──────
    protected int createGameInPast(List<Map<String, Object>> rosters, SquadSetup squad, int hoursAgo) {
        int gameId = createGameInFuture(rosters, squad);
        jdbc.update("UPDATE game SET date_time = ? WHERE id = ?",
                java.time.LocalDateTime.now().minusHours(hoursAgo), gameId);
        return gameId;
    }

    protected int createGameInFuture(List<Map<String, Object>> rosters, SquadSetup squad) {
        Map<String, Object> gameReq = new java.util.HashMap<>();
        gameReq.put("location", String.valueOf(squad.gameLocationId()));
        gameReq.put("weather", "SUNNY");
        gameReq.put("dateTime", java.time.LocalDateTime.now().plusHours(5).toString());
        gameReq.put("teamSize", rosters.size() / 2);
        gameReq.put("rosters", rosters);
        postWithSquad("/api/games/admin/createGame", gameReq, squad.adminToken(), squad.squadId(), String.class);
        return jdbc.queryForObject(
                "SELECT id FROM game WHERE squad_id = ? ORDER BY id DESC LIMIT 1",
                Integer.class, squad.squadId());
    }

    // ─── Helper: Gol ekle → isPlayed = true ──────────────────────────────────────
    protected void addGoals(int gameId, int scorerId, String teamColor, SquadSetup squad) {
        Map<String, Object> goalReq = new HashMap<>();
        goalReq.put("gameId", gameId);
        goalReq.put("goals", List.of(Map.of("playerId", scorerId, "teamColor", teamColor)));
        postWithSquad("/api/goals/admin/addGoals", goalReq, squad.adminToken(), squad.squadId(), String.class);
    }

    // ─── Helper: Roster ID'yi DB'den al ──────────────────────────────────────────
    protected int getRosterId(int gameId, int playerId) {
        return jdbc.queryForObject(
                "SELECT id FROM roster WHERE game_id = ? AND player_id = ?",
                Integer.class, gameId, playerId);
    }

    // ─── Helper: Oy ver ───────────────────────────────────────────────────────────
    protected void castVote(String voterToken, int squadId, List<Map<String, Object>> ratings) {
        postWithSquad("/api/ratings/saveRatings", ratings, voterToken, squadId, String.class);
    }

    // ─── Helper: Roster DTO oluştur ───────────────────────────────────────────────
    protected Map<String, Object> rosterEntry(int playerId, String teamColor) {
        return Map.of("playerId", playerId, "teamColor", teamColor);
    }

    // ─── Helper: Rating DTO oluştur ───────────────────────────────────────────────
    protected Map<String, Object> ratingEntry(int playerId, int rosterId, int rate) {
        return Map.of("playerId", playerId, "rosterId", rosterId, "rate", rate);
    }

    // ─── Record Types ─────────────────────────────────────────────────────────────
    public record UserSession(String username, String token) {}

    public record SquadSetup(UserSession admin, int squadId, String inviteCode, int adminPlayerId, int gameLocationId) {
        public String adminToken() { return admin.token(); }
    }

    public record MemberSetup(UserSession user, int playerId) {
        public String token() { return user.token(); }
    }
}
