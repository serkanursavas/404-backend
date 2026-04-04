package com.squad.squad.integration.game;

import com.squad.squad.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Grup C — Maç Lifecycle
 * Maç oluşturma, güncelleme, silme, gol ekleme, skor güncelleme, state machine
 */
@DisplayName("Game Lifecycle Integration Tests")
class GameLifecycleIntegrationTest extends BaseIntegrationTest {

    UserSession superAdmin;
    SquadSetup squad;
    MemberSetup player2, player3, player4;
    List<Map<String, Object>> rosters2v2;

    @BeforeEach
    void setupSquadWith4Players() {
        superAdmin = registerSuperAdmin("superadmin");
        squad = setupSquad("testadmin", superAdmin);
        player2 = joinSquad("oyuncu2", squad, squad.admin());
        player3 = joinSquad("oyuncu3", squad, squad.admin());
        player4 = joinSquad("oyuncu4", squad, squad.admin());

        rosters2v2 = List.of(
                rosterEntry(squad.adminPlayerId(), "BLACK"),
                rosterEntry(player2.playerId(), "BLACK"),
                rosterEntry(player3.playerId(), "WHITE"),
                rosterEntry(player4.playerId(), "WHITE")
        );
    }

    // ─── C1: Maç oluşturma ───────────────────────────────────────────────────────
    @Test
    @DisplayName("C1 — Admin 2v2 maç oluşturabilmeli")
    void createGame_withRosters_returns200AndGameId() {
        ResponseEntity<String> res = postWithSquad(
                "/api/games/admin/createGame",
                buildFutureGameReq(rosters2v2),
                squad.adminToken(), squad.squadId(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        int gameId = latestGameId();
        int rosterCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM roster WHERE game_id = ?", Integer.class, gameId);
        assertThat(rosterCount).isEqualTo(4);
    }

    // ─── C2: Oylanmamış maç varken yeni maç engeli ──────────────────────────────
    @Test
    @DisplayName("C2 — Oylanmamış bekleyen maç varken yeni maç 409 döndürmeli")
    void createGame_withExistingUnvotedGame_returns409() {
        // İlk maç
        postWithSquad("/api/games/admin/createGame",
                buildFutureGameReq(rosters2v2),
                squad.adminToken(), squad.squadId(), String.class);

        // İkinci maç → 409
        ResponseEntity<String> res = postWithSquad(
                "/api/games/admin/createGame",
                buildFutureGameReq(rosters2v2),
                squad.adminToken(), squad.squadId(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ─── C3: Maç oluşturma → rating temizleme ───────────────────────────────────
    @Test
    @DisplayName("C3 — Yeni maç oluşturulunca eski rating'ler silinmeli")
    void createGame_clearsOldRatings() {
        postWithSquad("/api/games/admin/createGame",
                buildFutureGameReq(rosters2v2),
                squad.adminToken(), squad.squadId(), String.class);

        int ratingCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM rating r " +
                "JOIN roster ro ON ro.id = r.roster_id " +
                "JOIN game g ON g.id = ro.game_id " +
                "WHERE g.squad_id = ?",
                Integer.class, squad.squadId());
        assertThat(ratingCount).isEqualTo(0);
    }

    // ─── C4: Maç silme → cascade ────────────────────────────────────────────────
    @Test
    @DisplayName("C4 — Maç silinince roster'lar da silinmeli")
    void deleteGame_cascadesRosters() {
        postWithSquad("/api/games/admin/createGame",
                buildFutureGameReq(rosters2v2),
                squad.adminToken(), squad.squadId(), String.class);
        int gameId = latestGameId();

        ResponseEntity<Void> deleteRes = deleteWithSquad(
                "/api/games/admin/deleteGame/" + gameId, squad.adminToken(), squad.squadId(), Void.class);
        assertThat(deleteRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        int rosterCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM roster WHERE game_id = ?", Integer.class, gameId);
        assertThat(rosterCount).isEqualTo(0);

        int gameCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM game WHERE id = ?", Integer.class, gameId);
        assertThat(gameCount).isEqualTo(0);
    }

    // ─── C5: Oynanmış maç güncellenemez ─────────────────────────────────────────
    @Test
    @DisplayName("C5 — isPlayed=true olan maç güncellenemez (409)")
    void updateGame_alreadyPlayed_returns409() {
        int gameId = createGameInPast(rosters2v2, squad, 2);
        addGoals(gameId, squad.adminPlayerId(), "BLACK", squad);

        // Minimum update request — just change weather (no teamSize/rosters to avoid validation)
        Map<String, Object> updateReq = new HashMap<>();
        updateReq.put("id", gameId);

        ResponseEntity<String> updateRes = putWithSquad(
                "/api/games/admin/updateGame/" + gameId, updateReq,
                squad.adminToken(), squad.squadId(), String.class);

        assertThat(updateRes.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ─── C6: Gol ekleme → BLACK skor güncelleme ──────────────────────────────────
    @Test
    @DisplayName("C6 — BLACK takım golü homeTeamScore artırmalı + isPlayed=true yapmalı")
    void addGoal_blackTeam_incrementsHomeScoreAndSetsPlayed() {
        int gameId = createGameInPast(rosters2v2, squad, 1);
        addGoals(gameId, squad.adminPlayerId(), "BLACK", squad);

        Map<String, Object> game = jdbc.queryForMap(
                "SELECT home_team_score, is_played FROM game WHERE id = ?", gameId);
        assertThat(game.get("home_team_score")).isEqualTo(1);
        assertThat(game.get("is_played")).isEqualTo(true);
    }

    // ─── C7: Gol ekleme → WHITE skor güncelleme ──────────────────────────────────
    @Test
    @DisplayName("C7 — WHITE takım golü awayTeamScore artırmalı")
    void addGoal_whiteTeam_incrementsAwayScore() {
        int gameId = createGameInPast(rosters2v2, squad, 1);
        addGoals(gameId, player3.playerId(), "WHITE", squad);

        Map<String, Object> game = jdbc.queryForMap(
                "SELECT away_team_score, home_team_score FROM game WHERE id = ?", gameId);
        assertThat(game.get("away_team_score")).isEqualTo(1);
        assertThat(game.get("home_team_score")).isEqualTo(0);
    }

    // ─── C8: Auto-mark played ────────────────────────────────────────────────────
    @Test
    @DisplayName("C8 — Geçmiş tarihli maç getirince isPlayed=true yapılmalı (auto-mark)")
    void getNextGame_pastDateGame_autoMarkedAsPlayed() {
        int gameId = createGameInPast(rosters2v2, squad, 3);

        Boolean isPlayedBefore = jdbc.queryForObject(
                "SELECT is_played FROM game WHERE id = ?", Boolean.class, gameId);
        assertThat(isPlayedBefore).isFalse();

        getWithSquad("/api/games/getNextGame", squad.adminToken(), squad.squadId(), Map.class);

        Boolean isPlayedAfter = jdbc.queryForObject(
                "SELECT is_played FROM game WHERE id = ?", Boolean.class, gameId);
        assertThat(isPlayedAfter).isTrue();
    }

    // ─── Yardımcılar ─────────────────────────────────────────────────────────────
    private Map<String, Object> buildFutureGameReq(List<Map<String, Object>> rosters) {
        Map<String, Object> req = new HashMap<>();
        req.put("location", String.valueOf(squad.gameLocationId()));
        req.put("weather", "SUNNY");
        req.put("dateTime", LocalDateTime.now().plusHours(5).toString());
        req.put("teamSize", rosters.size() / 2);
        req.put("rosters", rosters);
        return req;
    }

    private int latestGameId() {
        return jdbc.queryForObject(
                "SELECT id FROM game WHERE squad_id = ? ORDER BY id DESC LIMIT 1",
                Integer.class, squad.squadId());
    }
}
