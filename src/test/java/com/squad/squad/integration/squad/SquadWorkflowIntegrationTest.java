package com.squad.squad.integration.squad;

import com.squad.squad.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Grup B — Squad Workflow
 * Onboarding (squad oluşturma + katılma), admin işlemleri, izolasyon
 */
@DisplayName("Squad Workflow Integration Tests")
class SquadWorkflowIntegrationTest extends BaseIntegrationTest {

    UserSession superAdmin;

    @BeforeEach
    void setupSuperAdmin() {
        superAdmin = registerSuperAdmin("superadmin");
    }

    // ─── B1: Squad oluşturma talebi ──────────────────────────────────────────────
    @Test
    @DisplayName("B1 — Kullanıcı squad oluşturma talebi gönderebilmeli")
    void requestCreateSquad_authenticated_returns200() {
        UserSession user = registerUser("squadkurucu");

        Map<String, Object> req = Map.of(
                "squadName", "Test Squad",
                "playerName", "Ali", "playerSurname", "Veli",
                "playerPosition", "FW", "playerFoot", "RIGHT"
        );

        ResponseEntity<String> res = post("/api/squads/request-create", req, user.token(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        // DB'de PENDING kayıt var mı
        int count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM squad_request WHERE status = 'PENDING'", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    // ─── B2: Çift talep engeli ───────────────────────────────────────────────────
    @Test
    @DisplayName("B2 — PENDING talep varken ikinci talep 409 döndürmeli")
    void requestCreateSquad_alreadyPending_returns409() {
        UserSession user = registerUser("cifttalep");

        Map<String, Object> req = Map.of(
                "squadName", "Squad 1",
                "playerName", "Ali", "playerSurname", "Veli",
                "playerPosition", "FW", "playerFoot", "RIGHT"
        );

        post("/api/squads/request-create", req, user.token(), String.class);

        // İkinci talep
        Map<String, Object> req2 = Map.of(
                "squadName", "Squad 2",
                "playerName", "Ali", "playerSurname", "Veli",
                "playerPosition", "FW", "playerFoot", "RIGHT"
        );
        ResponseEntity<String> res2 = post("/api/squads/request-create", req2, user.token(), String.class);

        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ─── B3: SuperAdmin onaylama → Squad + Player + Membership oluştu mu ─────────
    @Test
    @DisplayName("B3 — SuperAdmin onaylayınca Squad + Player + GroupMembership oluşmalı")
    void approveSquadRequest_createsSquadPlayerAndMembership() {
        UserSession user = registerUser("yeniadmin");

        Map<String, Object> req = Map.of(
                "squadName", "Yeni Squad", "playerName", "Mehmet",
                "playerSurname", "Demir", "playerPosition", "MF", "playerFoot", "LEFT"
        );
        post("/api/squads/request-create", req, user.token(), String.class);

        // SuperAdmin pending listeyi al → requestId
        ResponseEntity<List> pending = get("/api/squads/super/pending-requests", superAdmin.token(), List.class);
        Integer requestId = (Integer) ((Map<?, ?>) pending.getBody().get(0)).get("id");

        // Onayla
        ResponseEntity<String> approveRes = put("/api/squads/super/approve/" + requestId, null, superAdmin.token(), String.class);
        assertThat(approveRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        // DB Doğrulama: Squad oluştu mu?
        int squadCount = jdbc.queryForObject("SELECT COUNT(*) FROM squad WHERE name = 'Yeni Squad'", Integer.class);
        assertThat(squadCount).isEqualTo(1);

        // Player oluştu mu?
        int playerCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM player WHERE name = 'Mehmet' AND surname = 'Demir'", Integer.class);
        assertThat(playerCount).isEqualTo(1);

        // GroupMembership oluştu mu ve ADMIN rolünde mi?
        String role = jdbc.queryForObject(
                "SELECT gm.role FROM group_membership gm " +
                "JOIN \"user\" u ON u.id = gm.user_id " +
                "WHERE u.username = 'yeniadmin'",
                String.class);
        assertThat(role).isEqualTo("ADMIN");

        // SquadRequest status APPROVED mi?
        String status = jdbc.queryForObject(
                "SELECT status FROM squad_request WHERE id = ?", String.class, requestId);
        assertThat(status).isEqualTo("APPROVED");
    }

    // ─── B4: SuperAdmin reddetme ─────────────────────────────────────────────────
    @Test
    @DisplayName("B4 — SuperAdmin reddetince status REJECTED olmalı, Squad oluşmamalı")
    void rejectSquadRequest_setsStatusRejected_noSquadCreated() {
        UserSession user = registerUser("reddedilecek");

        Map<String, Object> req = Map.of(
                "squadName", "Red Squad", "playerName", "X",
                "playerSurname", "Y", "playerPosition", "GK", "playerFoot", "RIGHT"
        );
        post("/api/squads/request-create", req, user.token(), String.class);

        ResponseEntity<List> pending = get("/api/squads/super/pending-requests", superAdmin.token(), List.class);
        Integer requestId = (Integer) ((Map<?, ?>) pending.getBody().get(0)).get("id");

        put("/api/squads/super/reject/" + requestId, null, superAdmin.token(), String.class);

        String status = jdbc.queryForObject(
                "SELECT status FROM squad_request WHERE id = ?", String.class, requestId);
        assertThat(status).isEqualTo("REJECTED");

        int squadCount = jdbc.queryForObject("SELECT COUNT(*) FROM squad", Integer.class);
        assertThat(squadCount).isEqualTo(0);
    }

    // ─── B5: Geçersiz invite code ────────────────────────────────────────────────
    @Test
    @DisplayName("B5 — Geçersiz invite code ile join talebi 404 döndürmeli")
    void requestJoinSquad_invalidInviteCode_returns404() {
        UserSession user = registerUser("jointest");

        Map<String, Object> req = Map.of(
                "inviteCode", "YANLISCODE",
                "playerName", "A", "playerSurname", "B",
                "playerPosition", "FW", "playerFoot", "RIGHT"
        );
        ResponseEntity<String> res = post("/api/squads/request-join", req, user.token(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── B6 + B7: Geçerli join → admin onayı → üye olundu ───────────────────────
    @Test
    @DisplayName("B6+B7 — Geçerli join talebi + admin onayı → Player + Membership oluşmalı")
    void requestJoinSquad_validCode_thenAdminApproves_createsMembership() {
        SquadSetup squad = setupSquad("testadmin", superAdmin);
        UserSession newMember = registerUser("yeniuye");

        // Join talebi
        Map<String, Object> req = Map.of(
                "inviteCode", squad.inviteCode(),
                "playerName", "Yeni", "playerSurname", "Üye",
                "playerPosition", "MF", "playerFoot", "LEFT"
        );
        ResponseEntity<String> joinRes = post("/api/squads/request-join", req, newMember.token(), String.class);
        assertThat(joinRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Admin join taleplerini listele
        ResponseEntity<List> joinReqs = getWithSquad(
                "/api/squads/admin/join-requests", squad.adminToken(), squad.squadId(), List.class);
        assertThat(joinReqs.getBody()).hasSize(1);

        Integer joinRequestId = (Integer) ((Map<?, ?>) joinReqs.getBody().get(0)).get("id");

        // Onayla
        ResponseEntity<String> approveRes = putWithSquad(
                "/api/squads/admin/approve-join/" + joinRequestId, null,
                squad.adminToken(), squad.squadId(), String.class);
        assertThat(approveRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        // DB: Yeni player oluştu mu?
        int playerCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM player p " +
                "JOIN group_membership gm ON gm.player_id = p.id " +
                "JOIN \"user\" u ON u.id = gm.user_id " +
                "WHERE gm.squad_id = ? AND u.username = 'yeniuye'",
                Integer.class, squad.squadId());
        assertThat(playerCount).isEqualTo(1);

        // Role MEMBER mi?
        String role = jdbc.queryForObject(
                "SELECT gm.role FROM group_membership gm " +
                "JOIN \"user\" u ON u.id = gm.user_id " +
                "WHERE gm.squad_id = ? AND u.username = 'yeniuye'",
                String.class, squad.squadId());
        assertThat(role).isEqualTo("MEMBER");
    }

    // ─── B8: Join talebi reddi ───────────────────────────────────────────────────
    @Test
    @DisplayName("B8 — Admin join talebini reddedince status REJECTED olmalı")
    void rejectJoinRequest_setsStatusRejected() {
        SquadSetup squad = setupSquad("adminred", superAdmin);
        UserSession applicant = registerUser("red_edilecek");

        Map<String, Object> req = Map.of(
                "inviteCode", squad.inviteCode(),
                "playerName", "A", "playerSurname", "B",
                "playerPosition", "FW", "playerFoot", "RIGHT"
        );
        post("/api/squads/request-join", req, applicant.token(), String.class);

        ResponseEntity<List> joinReqs = getWithSquad(
                "/api/squads/admin/join-requests", squad.adminToken(), squad.squadId(), List.class);
        Integer joinRequestId = (Integer) ((Map<?, ?>) joinReqs.getBody().get(0)).get("id");

        putWithSquad("/api/squads/admin/reject-join/" + joinRequestId, null,
                squad.adminToken(), squad.squadId(), String.class);

        String status = jdbc.queryForObject(
                "SELECT status FROM join_request WHERE id = ?", String.class, joinRequestId);
        assertThat(status).isEqualTo("REJECTED");
    }

    // ─── B9: 3 red → bloke ───────────────────────────────────────────────────────
    @Test
    @DisplayName("B9 — 3 red sonrası aynı squad'a katılma talebi 409 döndürmeli")
    void requestJoinSquad_after3Rejections_returns409() {
        SquadSetup squad = setupSquad("ucredadmin", superAdmin);
        UserSession applicant = registerUser("ucreduye");

        for (int i = 0; i < 3; i++) {
            // Talep gönder
            Map<String, Object> req = Map.of(
                    "inviteCode", squad.inviteCode(),
                    "playerName", "A", "playerSurname", "B",
                    "playerPosition", "FW", "playerFoot", "RIGHT"
            );
            post("/api/squads/request-join", req, applicant.token(), String.class);

            // Admin reddet
            ResponseEntity<List> joinReqs = getWithSquad(
                    "/api/squads/admin/join-requests", squad.adminToken(), squad.squadId(), List.class);
            Integer joinRequestId = (Integer) ((Map<?, ?>) joinReqs.getBody()
                    .stream()
                    .filter(r -> "PENDING".equals(((Map<?, ?>) r).get("status")))
                    .findFirst().orElseThrow()).get("id");

            putWithSquad("/api/squads/admin/reject-join/" + joinRequestId, null,
                    squad.adminToken(), squad.squadId(), String.class);
        }

        // 4. talep → bloke
        Map<String, Object> req4 = Map.of(
                "inviteCode", squad.inviteCode(),
                "playerName", "A", "playerSurname", "B",
                "playerPosition", "FW", "playerFoot", "RIGHT"
        );
        ResponseEntity<String> blockedRes = post("/api/squads/request-join", req4, applicant.token(), String.class);
        assertThat(blockedRes.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ─── B10: Son admin koruması ─────────────────────────────────────────────────
    @Test
    @DisplayName("B10 — Son admini MEMBER'a düşürmek 409 döndürmeli")
    void updateMemberRole_lastAdmin_returns409() {
        SquadSetup squad = setupSquad("tekadmin", superAdmin);

        // Admin user'ın userId'sini bul
        Integer adminUserId = jdbc.queryForObject(
                "SELECT u.id FROM \"user\" u WHERE u.username = ?",
                Integer.class, squad.admin().username());

        // Kendini MEMBER yapmaya çalış
        Map<String, Object> req = Map.of("role", "MEMBER");
        ResponseEntity<String> res = putWithSquad(
                "/api/squads/admin/update-role/" + adminUserId, req,
                squad.adminToken(), squad.squadId(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ─── B11: Üye kaldırma ───────────────────────────────────────────────────────
    @Test
    @DisplayName("B11 — Admin üyeyi kaldırınca GroupMembership silinmeli")
    void removeMember_memberRemoved_membershipDeleted() {
        SquadSetup squad = setupSquad("removeadmin", superAdmin);
        MemberSetup member = joinSquad("kaldirilacak", squad, squad.admin());

        Integer memberUserId = jdbc.queryForObject(
                "SELECT u.id FROM \"user\" u WHERE u.username = 'kaldirilacak'", Integer.class);

        ResponseEntity<String> res = deleteWithSquad(
                "/api/squads/admin/remove-member/" + memberUserId,
                squad.adminToken(), squad.squadId(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        int membershipCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM group_membership WHERE squad_id = ? AND user_id = ?",
                Integer.class, squad.squadId(), memberUserId);
        assertThat(membershipCount).isEqualTo(0);
    }

    // ─── B12: Squad data izolasyonu ──────────────────────────────────────────────
    @Test
    @DisplayName("B12 — Squad B kullanıcısı Squad A'nın maçlarına erişememeli (403)")
    void squadDataIsolation_squadBCannotAccessSquadAData() {
        SquadSetup squadA = setupSquad("admina", superAdmin);
        SquadSetup squadB = setupSquad("adminb", superAdmin);

        // Squad A'da bir maç oluştur (gerçek gameId gerekiyor ama squad context kontrolü yeterli)
        // Squad B kullanıcısı Squad A'nın endpoint'ini Squad A ID'siyle çağırmaya çalışır
        // GroupContextFilter: squadB user'ı squadA'da member değil → 403
        ResponseEntity<String> res = getWithSquad(
                "/api/games/getAllGames", squadB.adminToken(), squadA.squadId(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── B13: Invite code yenileme ───────────────────────────────────────────────
    @Test
    @DisplayName("B13 — Invite code yenilenince eski kod geçersiz olmalı")
    void regenerateInviteCode_oldCodeBecomesInvalid() {
        SquadSetup squad = setupSquad("inviteadmin", superAdmin);
        String oldCode = squad.inviteCode();

        // Invite code yenile
        postWithSquad("/api/squads/admin/regenerate-invite", null,
                squad.adminToken(), squad.squadId(), String.class);

        // Yeni kodu DB'den al
        String newCode = jdbc.queryForObject(
                "SELECT invite_code FROM squad WHERE id = ?", String.class, squad.squadId());

        assertThat(newCode).isNotEqualTo(oldCode);

        // Eski kod ile join talebi → 404
        UserSession user = registerUser("eskikodtest");
        Map<String, Object> req = Map.of(
                "inviteCode", oldCode,
                "playerName", "A", "playerSurname", "B",
                "playerPosition", "FW", "playerFoot", "RIGHT"
        );
        ResponseEntity<String> joinRes = post("/api/squads/request-join", req, user.token(), String.class);
        assertThat(joinRes.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
