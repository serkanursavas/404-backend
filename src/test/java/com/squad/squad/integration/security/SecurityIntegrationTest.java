package com.squad.squad.integration.security;

import com.squad.squad.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.net.URI;
import java.util.Map;

import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Grup F — Güvenlik & SSRF
 * SSRF whitelist kontrolü, rol tabanlı erişim, cross-squad izolasyon
 */
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest extends BaseIntegrationTest {

    UserSession superAdmin;
    SquadSetup squad;
    MemberSetup member;

    @BeforeEach
    void setup() {
        superAdmin = registerSuperAdmin("superadmin");
        squad = setupSquad("secadmin", superAdmin);
        member = joinSquad("secmember", squad, squad.admin());
    }

    // ─── F1: SSRF — localhost bloke ──────────────────────────────────────────────
    @Test
    @DisplayName("F1 — localhost URL SSRF saldırısı 400 döndürmeli")
    void resolveUrl_localhost_returns400() {
        ResponseEntity<String> res = resolveUrl("http://localhost:6379", squad.adminToken(), squad.squadId());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── F2: SSRF — internal IP bloke ────────────────────────────────────────────
    @Test
    @DisplayName("F2 — Internal IP SSRF saldırısı 400 döndürmeli")
    void resolveUrl_internalIp_returns400() {
        ResponseEntity<String> res = resolveUrl("http://192.168.1.1/admin", squad.adminToken(), squad.squadId());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── F3: SSRF — HTTP protokolü bloke (HTTPS zorunlu) ────────────────────────
    @Test
    @DisplayName("F3 — HTTP (non-HTTPS) ile whitelist domain 400 döndürmeli")
    void resolveUrl_httpNotHttps_returns400() {
        ResponseEntity<String> res = resolveUrl("http://maps.app.goo.gl/test", squad.adminToken(), squad.squadId());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── F4: SSRF — whitelist domain (HTTPS) izin ────────────────────────────────
    @Test
    @DisplayName("F4 — HTTPS + whitelist domain başarıyla işlenmeli (whitelist geçti)")
    void resolveUrl_httpsWhitelistDomain_notBlocked() {
        ResponseEntity<String> res = resolveUrl("https://maps.app.goo.gl/test123", squad.adminToken(), squad.squadId());
        // 400 BAD_REQUEST DEĞİL — whitelist geçti (5xx veya başka bir şey olabilir ama 400 değil)
        assertThat(res.getStatusCode()).isNotEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── F5: SSRF — kötü domain bloke ────────────────────────────────────────────
    @Test
    @DisplayName("F5 — Whitelist dışı HTTPS domain 400 döndürmeli")
    void resolveUrl_nonWhitelistDomain_returns400() {
        ResponseEntity<String> res = resolveUrl("https://evil.com/steal-data", squad.adminToken(), squad.squadId());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── F6: Admin endpoint → normal member ──────────────────────────────────────
    @Test
    @DisplayName("F6 — Normal MEMBER kullanıcısı admin endpoint çağırırsa 403 almalı")
    void adminEndpoint_calledByMember_returns403() {
        // Maç oluşturma ADMIN gerektirir
        Map<String, Object> gameReq = Map.of(
                "location", "Saha",
                "weather", "SUNNY",
                "dateTime", "2099-01-01T10:00:00",
                "teamSize", 2,
                "rosters", java.util.List.of()
        );

        ResponseEntity<String> res = postWithSquad(
                "/api/games/admin/createGame", gameReq,
                member.token(), squad.squadId(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── F7: SuperAdmin endpoint → admin ─────────────────────────────────────────
    @Test
    @DisplayName("F7 — Squad admin SuperAdmin endpoint çağırırsa 403 almalı")
    void superAdminEndpoint_calledByAdmin_returns403() {
        // Squad admin "tüm squads" listesini çekmeye çalışır (SuperAdmin gerekli)
        ResponseEntity<String> res = get(
                "/api/squads/super/all-squads", squad.adminToken(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── F8: Cross-squad erişim ──────────────────────────────────────────────────
    @Test
    @DisplayName("F8 — Squad B kullanıcısı Squad A ID ile istek yaparsa 403 almalı")
    void crossSquadAccess_squadBTokenWithSquadAId_returns403() {
        SquadSetup squadB = setupSquad("squadbadmin", superAdmin);

        // Squad B admini, Squad A'nın ID'si ile istek gönderir
        ResponseEntity<String> res = getWithSquad(
                "/api/games/getNextGame", squadB.adminToken(), squad.squadId(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────────
    private ResponseEntity<String> resolveUrl(String rawUrl, String token, int squadId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(url("/api/util/resolve-url"))
                .queryParam("url", rawUrl)
                .build().toUri();
        return http.exchange(uri, HttpMethod.GET, new HttpEntity<>(squadHeaders(token, squadId)), String.class);
    }
}
