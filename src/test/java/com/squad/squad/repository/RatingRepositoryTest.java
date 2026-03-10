package com.squad.squad.repository;

import com.squad.squad.entity.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
class RatingRepositoryTest {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RosterRepository rosterRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setupSecurity() {
        // AuditorAwareImpl only creates a User reference when principal is CustomUserDetails.
        // A plain String principal falls through to Optional.empty(), so createdBy stays null.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test", null, List.of()));
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Squad createSquad(String name, String inviteCode) {
        Squad squad = new Squad();
        squad.setName(name);
        squad.setInviteCode(inviteCode);
        entityManager.persist(squad);
        return squad;
    }

    private Player createPlayer(Squad squad) {
        Player player = new Player();
        player.setSquad(squad);
        entityManager.persist(player);
        return player;
    }

    private Game createGame(Squad squad, boolean played, boolean voted) {
        Game game = new Game();
        game.setSquad(squad);
        game.setPlayed(played);
        game.setVoted(voted);
        game.setLocation("Test Saha");
        game.setWeather("Acik");
        game.setDateTime(LocalDateTime.now());
        entityManager.persist(game);
        return game;
    }

    private Roster createRoster(Game game, Player player, String color) {
        Roster roster = new Roster();
        roster.setGame(game);
        roster.setPlayer(player);
        roster.setTeamColor(color);
        entityManager.persist(roster);
        return roster;
    }

    private Rating createRating(Roster roster, Player ratingPlayer, int rate) {
        Rating rating = new Rating();
        rating.setRoster(roster);
        rating.setPlayer(ratingPlayer);
        rating.setRate(rate);
        entityManager.persist(rating);
        return rating;
    }

    // ── existsByPlayerIdAndActiveGame ──────────────────────────────────────────

    @Test
    void existsByPlayerIdAndActiveGame_playedNotVoted_returnsTrue() {
        Squad squad = createSquad("Squad1", "TSTCD001");
        Player player = createPlayer(squad);
        Game game = createGame(squad, true, false);
        Roster roster = createRoster(game, player, "WHITE");
        createRating(roster, player, 8);
        entityManager.flush();

        assertThat(ratingRepository.existsByPlayerIdAndActiveGame(player.getId(), squad.getId()))
                .isTrue();
    }

    @Test
    void existsByPlayerIdAndActiveGame_playedAlreadyVoted_returnsFalse() {
        Squad squad = createSquad("Squad1", "TSTCD002");
        Player player = createPlayer(squad);
        Game game = createGame(squad, true, true); // voted = true
        Roster roster = createRoster(game, player, "WHITE");
        createRating(roster, player, 8);
        entityManager.flush();

        assertThat(ratingRepository.existsByPlayerIdAndActiveGame(player.getId(), squad.getId()))
                .isFalse();
    }

    @Test
    void existsByPlayerIdAndActiveGame_notPlayedYet_returnsFalse() {
        Squad squad = createSquad("Squad1", "TSTCD003");
        Player player = createPlayer(squad);
        Game game = createGame(squad, false, false); // not played
        Roster roster = createRoster(game, player, "WHITE");
        createRating(roster, player, 8);
        entityManager.flush();

        assertThat(ratingRepository.existsByPlayerIdAndActiveGame(player.getId(), squad.getId()))
                .isFalse();
    }

    @Test
    void existsByPlayerIdAndActiveGame_differentSquad_returnsFalse() {
        Squad squad1 = createSquad("Squad1", "TSTCD004");
        Squad squad2 = createSquad("Squad2", "TSTCD005");

        Player player = createPlayer(squad1);
        Game gameSquad1 = createGame(squad1, true, false);
        Roster roster = createRoster(gameSquad1, player, "WHITE");
        createRating(roster, player, 8);
        entityManager.flush();

        // Query with squad2 → player's rating is in squad1's game, not squad2's
        assertThat(ratingRepository.existsByPlayerIdAndActiveGame(player.getId(), squad2.getId()))
                .isFalse();
    }

    @Test
    void existsByPlayerIdAndActiveGame_playerWithNoRatings_returnsFalse() {
        Squad squad = createSquad("Squad1", "TSTCD006");
        Player player = createPlayer(squad);
        entityManager.flush();

        assertThat(ratingRepository.existsByPlayerIdAndActiveGame(player.getId(), squad.getId()))
                .isFalse();
    }

    // ── deleteAllBySquadId ─────────────────────────────────────────────────────

    @Test
    void deleteAllBySquadId_deletesOnlyTargetSquadRatings() {
        Squad squad1 = createSquad("Squad1", "TSTCD007");
        Squad squad2 = createSquad("Squad2", "TSTCD008");

        Player p1 = createPlayer(squad1);
        Player p2 = createPlayer(squad2);

        Game g1 = createGame(squad1, true, false);
        Game g2 = createGame(squad2, true, false);

        Roster r1 = createRoster(g1, p1, "WHITE");
        Roster r2 = createRoster(g2, p2, "WHITE");

        // Squad1: 3 ratings, Squad2: 2 ratings
        createRating(r1, p1, 7);
        createRating(r1, p1, 8);
        createRating(r1, p1, 9);
        createRating(r2, p2, 6);
        createRating(r2, p2, 7);

        entityManager.flush();

        assertThat(ratingRepository.countBySquadId(squad1.getId())).isEqualTo(3);
        assertThat(ratingRepository.countBySquadId(squad2.getId())).isEqualTo(2);

        ratingRepository.deleteAllBySquadId(squad1.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(ratingRepository.countBySquadId(squad1.getId())).isEqualTo(0);
        assertThat(ratingRepository.countBySquadId(squad2.getId())).isEqualTo(2);
    }

    // ── findRosterByPlayerIdAndSquadId ─────────────────────────────────────────

    @Test
    void findRosterByPlayerIdAndSquadId_returnsOnlyMatchingSquadRosters() {
        Squad squad1 = createSquad("Squad1", "TSTCD009");
        Squad squad2 = createSquad("Squad2", "TSTCD010");

        Player player = createPlayer(squad1);

        Game g1a = createGame(squad1, true, true);
        Game g1b = createGame(squad1, true, true);
        Game g2 = createGame(squad2, true, true);

        createRoster(g1a, player, "WHITE");
        createRoster(g1b, player, "BLACK");
        createRoster(g2, player, "WHITE");

        entityManager.flush();

        List<Roster> squad1Rosters = rosterRepository.findRosterByPlayerIdAndSquadId(
                player.getId(), squad1.getId());
        List<Roster> squad2Rosters = rosterRepository.findRosterByPlayerIdAndSquadId(
                player.getId(), squad2.getId());

        assertThat(squad1Rosters).hasSize(2);
        assertThat(squad2Rosters).hasSize(1);
    }
}
