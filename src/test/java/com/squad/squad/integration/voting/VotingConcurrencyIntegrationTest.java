package com.squad.squad.integration.voting;

import com.squad.squad.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Grup D — Concurrent Voting (D8–D9)
 *
 * Race condition testleri:
 * D8: Aynı anda 4 thread oy gönderir (her takımdan 2 voter) → tek sonuç beklenir
 * D9: Aynı voter 5 kez aynı anda POST → sadece 1 kabul edilmeli
 *
 * ratingEntry(playerId, rosterId, rate) semantiği:
 *   playerId = OY VEREN (voter) oyuncunun ID'si
 *   rosterId = OY ALINAN (voted) oyuncunun roster ID'si
 */
@DisplayName("Voting Concurrency Integration Tests")
class VotingConcurrencyIntegrationTest extends BaseIntegrationTest {

    UserSession superAdmin;
    SquadSetup squad;
    MemberSetup player2, player3, player4;
    int gameId;
    int roster_admin, roster_p2, roster_p3, roster_p4;

    @BeforeEach
    void setupGameFixture() {
        superAdmin = registerSuperAdmin("superadmin");
        squad = setupSquad("concadmin", superAdmin);
        player2 = joinSquad("concvoter2", squad, squad.admin());
        player3 = joinSquad("concvoter3", squad, squad.admin());
        player4 = joinSquad("concvoter4", squad, squad.admin());

        List<Map<String, Object>> rosters = List.of(
                rosterEntry(squad.adminPlayerId(), "BLACK"),
                rosterEntry(player2.playerId(), "BLACK"),
                rosterEntry(player3.playerId(), "WHITE"),
                rosterEntry(player4.playerId(), "WHITE")
        );

        gameId = createGameInPast(rosters, squad, 2);

        addGoals(gameId, squad.adminPlayerId(), "BLACK", squad);

        roster_admin = getRosterId(gameId, squad.adminPlayerId());
        roster_p2    = getRosterId(gameId, player2.playerId());
        roster_p3    = getRosterId(gameId, player3.playerId());
        roster_p4    = getRosterId(gameId, player4.playerId());
    }

    // ─── D8: Concurrent voting — 4 thread ───────────────────────────────────────
    @Test
    @DisplayName("D8 — 4 oyuncu aynı anda oy gönderirse sonuç tutarlı olmalı (race condition)")
    void concurrentVoting_4ThreadsAtOnce_consistentResult() throws InterruptedException {
        int threadCount = 4;
        CountDownLatch startGate = new CountDownLatch(1); // hepsini aynı anda serbest bırak
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // voter=admin → voted=player2's roster | voter=player2 → voted=admin's roster
        // voter=player3 → voted=player4's roster | voter=player4 → voted=player3's roster
        Runnable[] voters = {
                () -> {
                    List<Map<String, Object>> votes = List.of(
                            ratingEntry(squad.adminPlayerId(), roster_p2, 8));
                    ResponseEntity<String> res = postWithSquad(
                            "/api/ratings/saveRatings", votes, squad.adminToken(), squad.squadId(), String.class);
                    if (res.getStatusCode().is2xxSuccessful()) successCount.incrementAndGet();
                    else errorCount.incrementAndGet();
                },
                () -> {
                    List<Map<String, Object>> votes = List.of(
                            ratingEntry(player2.playerId(), roster_admin, 7));
                    ResponseEntity<String> res = postWithSquad(
                            "/api/ratings/saveRatings", votes, player2.token(), squad.squadId(), String.class);
                    if (res.getStatusCode().is2xxSuccessful()) successCount.incrementAndGet();
                    else errorCount.incrementAndGet();
                },
                () -> {
                    List<Map<String, Object>> votes = List.of(
                            ratingEntry(player3.playerId(), roster_p4, 9));
                    ResponseEntity<String> res = postWithSquad(
                            "/api/ratings/saveRatings", votes, player3.token(), squad.squadId(), String.class);
                    if (res.getStatusCode().is2xxSuccessful()) successCount.incrementAndGet();
                    else errorCount.incrementAndGet();
                },
                () -> {
                    List<Map<String, Object>> votes = List.of(
                            ratingEntry(player4.playerId(), roster_p3, 6));
                    ResponseEntity<String> res = postWithSquad(
                            "/api/ratings/saveRatings", votes, player4.token(), squad.squadId(), String.class);
                    if (res.getStatusCode().is2xxSuccessful()) successCount.incrementAndGet();
                    else errorCount.incrementAndGet();
                }
        };

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (Runnable voter : voters) {
            executor.submit(() -> {
                try {
                    startGate.await(); // hepsi aynı anda başlasın
                    voter.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGate.countDown(); // Ateş!
        done.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        // Tüm oylar kabul edilmeli (4 farklı voter, her biri farklı)
        assertThat(successCount.get()).isEqualTo(4);

        // DB: tam 4 rating kaydı var mı?
        int ratingCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM rating r " +
                "JOIN roster ro ON ro.id = r.roster_id " +
                "WHERE ro.game_id = ?",
                Integer.class, gameId);
        assertThat(ratingCount).isEqualTo(4);

        // Not: concurrent tx'ler altında cascade (is_voted) güvenilir biçimde tetiklenmeyebilir.
        // Cascade davranışı D2 (sequential) testinde doğrulanmaktadır.
    }

    // ─── D9: Duplicate submit — aynı voter 5 kez ────────────────────────────────
    @Test
    @DisplayName("D9 — Aynı voter 5 thread'den eş zamanlı oy gönderirse sadece 1 kabul edilmeli")
    void duplicateVoteSubmit_5ConcurrentRequests_onlyOneAccepted() throws InterruptedException {
        int threadCount = 5;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        // voter=admin, voted=player2's roster
        List<Map<String, Object>> votes = List.of(
                ratingEntry(squad.adminPlayerId(), roster_p2, 8));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    ResponseEntity<String> res = postWithSquad(
                            "/api/ratings/saveRatings", votes, squad.adminToken(), squad.squadId(), String.class);
                    if (res.getStatusCode().is2xxSuccessful()) successCount.incrementAndGet();
                    else rejectedCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGate.countDown();
        done.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        // DB: admin voter için yalnızca 1 rating kaydı olmalı
        // r.player_id = voter (admin), ro.player_id = voted player (player2), ro.game_id = gameId
        int adminVoteCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM rating r " +
                "JOIN roster ro ON ro.id = r.roster_id " +
                "WHERE ro.game_id = ? AND ro.player_id = ? AND r.player_id = ?",
                Integer.class, gameId, player2.playerId(), squad.adminPlayerId());

        assertThat(adminVoteCount)
                .as("Aynı voter birden fazla rating kaydı oluşturmamalı")
                .isEqualTo(1);

        // Admin'in roster'ında hasVote = true yalnızca bir kez
        Boolean hasVote = jdbc.queryForObject(
                "SELECT has_vote FROM roster WHERE id = ?", Boolean.class, roster_admin);
        assertThat(hasVote).isTrue();
    }
}
