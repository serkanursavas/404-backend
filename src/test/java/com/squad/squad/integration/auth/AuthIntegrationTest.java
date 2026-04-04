package com.squad.squad.integration.auth;

import com.squad.squad.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Grup A — Auth & Kullanıcı Yönetimi
 * Gerçek HTTP + gerçek PostgreSQL (TestContainers)
 */
@DisplayName("Auth Integration Tests")
class AuthIntegrationTest extends BaseIntegrationTest {

    // ─── A1: Başarılı kayıt ──────────────────────────────────────────────────────
    @Test
    @DisplayName("A1 — Yeni kullanıcı kaydı başarılı olmalı (201 + token)")
    void createUser_validCredentials_returns201AndToken() {
        Map<String, Object> body = Map.of(
                "username", "yenikullanici",
                "password", "Password1!",
                "passwordAgain", "Password1!"
        );

        ResponseEntity<Map> res = http.postForEntity(url("/api/users/createUser"), body, Map.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody()).containsKey("token");
        assertThat((String) res.getBody().get("token")).isNotBlank();
    }

    // ─── A2: Duplicate username ──────────────────────────────────────────────────
    @Test
    @DisplayName("A2 — Aynı username ile ikinci kayıt 409 döndürmeli")
    void createUser_duplicateUsername_returns409() {
        registerUser("testuser");

        Map<String, Object> body = Map.of(
                "username", "testuser",
                "password", "Password1!",
                "passwordAgain", "Password1!"
        );

        ResponseEntity<String> res = http.postForEntity(url("/api/users/createUser"), body, String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ─── A3: Başarılı login ──────────────────────────────────────────────────────
    @Test
    @DisplayName("A3 — Doğru credentials ile login token + squads döndürmeli")
    void login_correctCredentials_returnsTokenAndSquads() {
        registerUser("logintest");

        Map<String, Object> body = Map.of(
                "username", "logintest",
                "password", "Password1!"
        );

        ResponseEntity<Map> res = http.postForEntity(url("/api/users/login"), body, Map.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) res.getBody().get("token")).isNotBlank();
        assertThat(res.getBody()).containsKey("squads");
    }

    // ─── A4: Yanlış şifre ───────────────────────────────────────────────────────
    @Test
    @DisplayName("A4 — Yanlış şifre ile login 401 döndürmeli")
    void login_wrongPassword_returns401() {
        registerUser("wrongpassuser");

        Map<String, Object> body = Map.of(
                "username", "wrongpassuser",
                "password", "YanlisPass99!"
        );

        ResponseEntity<String> res = http.postForEntity(url("/api/users/login"), body, String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── A5: Token olmadan korumalı endpoint ────────────────────────────────────
    @Test
    @DisplayName("A5 — Token olmadan korumalı endpoint 401 döndürmeli")
    void protectedEndpoint_withoutToken_returns401() {
        ResponseEntity<String> res = http.getForEntity(url("/api/squads/my-squads"), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── A5b: Geçersiz token ─────────────────────────────────────────────────────
    @Test
    @DisplayName("A5b — Sahte token ile istek 401 döndürmeli")
    void protectedEndpoint_withFakeToken_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("bu.sahte.bir.token");
        ResponseEntity<String> res = http.exchange(
                url("/api/squads/my-squads"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── A6: Profil güncelleme ───────────────────────────────────────────────────
    @Test
    @DisplayName("A6 — Kullanıcı kendi profilini güncelleyebilmeli")
    void updateProfile_ownProfile_returns200() {
        UserSession user = registerUser("profiltest");

        Map<String, Object> update = Map.of(
                "username", "profiltest",
                "password", "YeniPassword2!"
        );

        ResponseEntity<String> res = http.exchange(
                url("/api/users/updateProfile/profiltest"),
                HttpMethod.PUT,
                new HttpEntity<>(update, authHeaders(user.token())),
                String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ─── A7: Başkasının profilini güncelleme ─────────────────────────────────────
    @Test
    @DisplayName("A7 — Başka kullanıcının profilini güncelleme 403 döndürmeli")
    void updateProfile_otherUsersProfile_returns403() {
        UserSession user1 = registerUser("kullanici1");
        registerUser("kullanici2");

        Map<String, Object> update = Map.of(
                "username", "kullanici2_yeni",
                "password", "YeniPassword2!"
        );

        // user1 token ile kullanici2'nin profilini güncellemeye çalış
        ResponseEntity<String> res = http.exchange(
                url("/api/users/updateProfile/kullanici2"),
                HttpMethod.PUT,
                new HttpEntity<>(update, authHeaders(user1.token())),
                String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── A8: Şifre sıfırlama yetkisiz ───────────────────────────────────────────
    @Test
    @DisplayName("A8 — Normal user şifre sıfırlama 403 döndürmeli")
    void resetPassword_asNormalUser_returns403() {
        UserSession user = registerUser("normaluser");
        registerUser("targetuser");

        ResponseEntity<String> res = post(
                "/api/users/admin/resetPassword/targetuser", null, user.token(), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
