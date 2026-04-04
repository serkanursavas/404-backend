package com.squad.squad.integration.voting;

import com.squad.squad.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Grup D — Voting & Rating Cascade (D1–D7, D11–D12)
 *
 * Senaryo: 2v2 maç (BLACK: admin + player2, WHITE: player3 + player4)
 * Her oyuncu kendi takımındaki diğer oyuncuya oy verir (teamSize=2 → 1 oy yeterli)
 *
 * Cascade zinciri:
 *   Her voter → saveRatings →
 *     [her takım tamamlandığında] updateRatingsForGame →
 *       [her iki takım tamamlandığında] updatePlayerGeneralRating + persona + MVP + isVoted=true
 *
 * ratingEntry(playerId, rosterId, rate) semantiği:
 *   playerId = OY VEREN (voter) oyuncunun ID'si
 *   rosterId = OY ALINAN (voted) oyuncunun roster ID'si
 */
@DisplayName("Voting Cascade Integration Tests")
class VotingCascadeIntegrationTest extends BaseIntegrationTest {

    UserSession superAdmin;
    SquadSetup squad;
    MemberSetup player2, player3, player4;
    int gameId;
    // Roster ID'leri
    int roster_admin, roster_p2, roster_p3, roster_p4;

    /**
     * Test fixture: squad + 4 oyuncu + 1 maç + goller (isPlayed=true)
     * BLACK: adminPlayer (rosterId=roster_admin), player2 (rosterId=roster_p2)
     * WHITE: player3 (rosterId=roster_p3), player4 (rosterId=roster_p4)
     */
    @BeforeEach
    void setupGameFixture() {
        superAdmin = registerSuperAdmin("superadmin");
        squad = setupSquad("votingadmin", superAdmin);
        player2 = joinSquad("voter2", squad, squad.admin());
        player3 = joinSquad("voter3", squad, squad.admin());
        player4 = joinSquad("voter4", squad, squad.admin());

        // 2v2 maç oluştur (geçmiş tarih → isPlayed auto-mark veya gol ile)
        List<Map<String, Object>> rosters = List.of(
                rosterEntry(squad.adminPlayerId(), "BLACK"),
                rosterEntry(player2.playerId(), "BLACK"),
                rosterEntry(player3.playerId(), "WHITE"),
                rosterEntry(player4.playerId(), "WHITE")
        );

        gameId = createGameInPast(rosters, squad, 2);

        // Gol ekle → isPlayed = true
        addGoals(gameId, squad.adminPlayerId(), "BLACK", squad);

        // Roster ID'lerini al
        roster_admin = getRosterId(gameId, squad.adminPlayerId());
        roster_p2    = getRosterId(gameId, player2.playerId());
        roster_p3    = getRosterId(gameId, player3.playerId());
        roster_p4    = getRosterId(gameId, player4.playerId());
    }

    // ─── D1: Başarılı oy (tek takım) ────────────────────────────────────────────
    @Test
    @DisplayName("D1 — BLACK takım oylaması tamamlanınca roster rating'leri hesaplanmalı")
    void blackTeamVoting_complete_rostersGetRatings() {
        // admin (voter) → player2'ye oy ver: playerId=admin, rosterId=player2's roster
        List<Map<String, Object>> adminVotes = List.of(
                ratingEntry(squad.adminPlayerId(), roster_p2, 8)
        );
        ResponseEntity<String> adminRes = castVoteAndReturn(squad.adminToken(), adminVotes);
        assertThat(adminRes.getStatusCode().is2xxSuccessful()).isTrue();

        // player2 (voter) → admin'e oy ver: playerId=player2, rosterId=admin's roster → BLACK tamamlandı
        List<Map<String, Object>> p2Votes = List.of(
                ratingEntry(player2.playerId(), roster_admin, 7)
        );
        castVoteAndReturn(player2.token(), p2Votes);

        // DB: BLACK roster'ların rating'i hesaplandı mı (null değil)?
        Double adminRating = jdbc.queryForObject(
                "SELECT rating FROM roster WHERE id = ?", Double.class, roster_admin);
        Double p2Rating = jdbc.queryForObject(
                "SELECT rating FROM roster WHERE id = ?", Double.class, roster_p2);

        assertThat(adminRating).isNotNull().isGreaterThan(0);
        assertThat(p2Rating).isNotNull().isGreaterThan(0);

        // isVoted hâlâ false (WHITE oy vermedi)
        Boolean isVoted = jdbc.queryForObject(
                "SELECT is_voted FROM game WHERE id = ?", Boolean.class, gameId);
        assertThat(isVoted).isFalse();
    }

    // ─── D2: Her iki takım oylaması → full cascade ───────────────────────────────
    @Test
    @DisplayName("D2 — Her iki takım oylaması tamamlanınca isVoted=true + MVP + player rating")
    void bothTeamsVote_fullCascade_gameVotedAndMvpSet() {
        // BLACK oylama
        castVoteAndReturn(squad.adminToken(), List.of(ratingEntry(squad.adminPlayerId(), roster_p2, 9)));
        castVoteAndReturn(player2.token(), List.of(ratingEntry(player2.playerId(), roster_admin, 8)));

        // WHITE oylama
        castVoteAndReturn(player3.token(), List.of(ratingEntry(player3.playerId(), roster_p4, 7)));
        castVoteAndReturn(player4.token(), List.of(ratingEntry(player4.playerId(), roster_p3, 6)));

        // DB Doğrulama: game.is_voted = true
        Boolean isVoted = jdbc.queryForObject(
                "SELECT is_voted FROM game WHERE id = ?", Boolean.class, gameId);
        assertThat(isVoted).isTrue();

        // Tüm roster'ların rating'i hesaplandı mı?
        int unratedRosters = jdbc.queryForObject(
                "SELECT COUNT(*) FROM roster WHERE game_id = ? AND rating IS NULL", Integer.class, gameId);
        assertThat(unratedRosters).isEqualTo(0);

        // Player.rating güncellendi mi? (0'dan farklı olmalı)
        Double adminPlayerRating = jdbc.queryForObject(
                "SELECT rating FROM player WHERE id = ?", Double.class, squad.adminPlayerId());
        assertThat(adminPlayerRating).isNotNull().isGreaterThan(0);
    }

    // ─── D3: Kendi oyunu verme engeli ────────────────────────────────────────────
    @Test
    @DisplayName("D3 — Oyuncu kendi roster ID'sine oy veremez")
    void saveRatings_selfVote_returnsError() {
        // Admin kendi roster ID'sine oy vermeye çalış:
        // playerId=admin, rosterId=admin's own roster → self-vote detected (voter == voted player's roster's player)
        List<Map<String, Object>> selfVote = List.of(
                ratingEntry(squad.adminPlayerId(), roster_admin, 8)
        );
        ResponseEntity<String> res = castVoteAndReturn(squad.adminToken(), selfVote);

        // 4xx hatası bekleniyor
        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
    }

    // ─── D4: Karşı takıma oy verme engeli ───────────────────────────────────────
    @Test
    @DisplayName("D4 — BLACK oyuncu WHITE takım oyuncusuna oy veremez")
    void saveRatings_crossTeamVote_returnsError() {
        // admin (BLACK voter) → player3 (WHITE) → 422/400
        // playerId=admin, rosterId=player3's roster (WHITE) → cross-team check fails
        List<Map<String, Object>> crossVote = List.of(
                ratingEntry(squad.adminPlayerId(), roster_p3, 7)
        );
        ResponseEntity<String> res = castVoteAndReturn(squad.adminToken(), crossVote);

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
    }

    // ─── D5: Duplikat oy engeli ──────────────────────────────────────────────────
    @Test
    @DisplayName("D5 — Aynı oyuncuya ikinci kez oy verme engellenmeli")
    void saveRatings_duplicateVote_returnsError() {
        // İlk oy: admin → player2
        castVoteAndReturn(squad.adminToken(), List.of(ratingEntry(squad.adminPlayerId(), roster_p2, 8)));

        // İkinci oy aynı player'a → hata (existsByPlayerIdAndRosterId check)
        ResponseEntity<String> res = castVoteAndReturn(
                squad.adminToken(), List.of(ratingEntry(squad.adminPlayerId(), roster_p2, 5)));

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
    }

    // ─── D6: Oynanmamış maçta oy engeli ─────────────────────────────────────────
    @Test
    @DisplayName("D6 — isPlayed=false maçta oy verme engellenmeli")
    void saveRatings_gameNotPlayed_returnsError() {
        // Yeni maç oluştur (gelecek tarihli → isPlayed=false)
        UserSession superAdmin2 = registerSuperAdmin("superadmin2");
        SquadSetup squad2 = setupSquad("admin2", superAdmin2);
        MemberSetup m2 = joinSquad("mem2", squad2, squad2.admin());
        MemberSetup m3 = joinSquad("mem3", squad2, squad2.admin());
        MemberSetup m4 = joinSquad("mem4", squad2, squad2.admin());

        List<Map<String, Object>> rosters = List.of(
                rosterEntry(squad2.adminPlayerId(), "BLACK"),
                rosterEntry(m2.playerId(), "BLACK"),
                rosterEntry(m3.playerId(), "WHITE"),
                rosterEntry(m4.playerId(), "WHITE")
        );
        int newGameId = createGameInFuture(rosters, squad2);

        // voter = squad2.admin, voted = m2 → m2's roster
        int r2 = getRosterId(newGameId, m2.playerId());

        // Oy vermeye çalış → hata bekleniyor (isPlayed=false)
        List<Map<String, Object>> vote = List.of(ratingEntry(squad2.adminPlayerId(), r2, 8));
        ResponseEntity<String> res = postWithSquad(
                "/api/ratings/saveRatings", vote, squad2.adminToken(), squad2.squadId(), String.class);

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
    }

    // ─── D7: Zaten oylanmış maçta oy engeli ─────────────────────────────────────
    @Test
    @DisplayName("D7 — isVoted=true maçta oy verme engellenmeli")
    void saveRatings_gameAlreadyVoted_returnsError() {
        // Tam oylama yap → isVoted=true
        castVoteAndReturn(squad.adminToken(), List.of(ratingEntry(squad.adminPlayerId(), roster_p2, 8)));
        castVoteAndReturn(player2.token(), List.of(ratingEntry(player2.playerId(), roster_admin, 7)));
        castVoteAndReturn(player3.token(), List.of(ratingEntry(player3.playerId(), roster_p4, 6)));
        castVoteAndReturn(player4.token(), List.of(ratingEntry(player4.playerId(), roster_p3, 5)));

        // Tekrar oy gönder → hata (isVoted=true)
        ResponseEntity<String> res = castVoteAndReturn(
                squad.adminToken(), List.of(ratingEntry(squad.adminPlayerId(), roster_p2, 8)));

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
    }

    // ─── D2 detay: MVP set edildi mi ─────────────────────────────────────────────
    // MVP, persona_id=68 oylarına göre hesaplanır.
    // Önce persona oylaması yapılır, sonra rating oylaması cascade'i tetikler.
    @Test
    @DisplayName("D2-MVP — Persona oylaması + rating oylaması sonrası game.mvp_id set edilmeli")
    void bothTeamsVote_mvpIdIsSet() {
        // Persona oylama: player2'nin roster'ına MVP (persona_id=68) oy ekle
        // PersonaController EXEMPT_PATHS'te olduğundan GroupContext null → direkt DB insert
        jdbc.update(
                "INSERT INTO roster_persona (roster_id, persona_id, count, active) VALUES (?, 68, 1, true)",
                roster_p2);

        // Rating oylaması (cascade'i tetikler)
        castVoteAndReturn(squad.adminToken(), List.of(ratingEntry(squad.adminPlayerId(), roster_p2, 9)));
        castVoteAndReturn(player2.token(), List.of(ratingEntry(player2.playerId(), roster_admin, 8)));
        castVoteAndReturn(player3.token(), List.of(ratingEntry(player3.playerId(), roster_p4, 7)));
        castVoteAndReturn(player4.token(), List.of(ratingEntry(player4.playerId(), roster_p3, 6)));

        // mvp_id set edildi mi?
        Integer mvpId = jdbc.queryForObject(
                "SELECT mvp_id FROM game WHERE id = ?", Integer.class, gameId);
        assertThat(mvpId).isNotNull();
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────
    private ResponseEntity<String> castVoteAndReturn(String token, List<Map<String, Object>> ratings) {
        return postWithSquad("/api/ratings/saveRatings", ratings, token, squad.squadId(), String.class);
    }
}
