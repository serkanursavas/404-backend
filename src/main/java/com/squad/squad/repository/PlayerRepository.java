package com.squad.squad.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.squad.squad.dto.TopListProjection;
import com.squad.squad.entity.Player;

@Repository
public interface PlayerRepository extends SecureJpaRepository<Player, Integer> {

        List<Player> findByActiveTrue();

        Optional<Player> findByIdAndActive(Integer id, boolean active);

        @Query("SELECT p FROM Player p WHERE p.groupId = :groupId AND p.groupId != 0")
        List<Player> findByGroupId(@Param("groupId") Integer groupId);

        @Query("SELECT p FROM Player p WHERE p.id = :playerId")
        List<Player> findByPlayerId(@Param("playerId") Integer playerId);

        @Query(value = "with TopRated as (SELECT p.id AS playerId, p.name, p.surname, p.rating\n" +
                        "                  FROM player p\n" +
                        "                  WHERE p.rating IS NOT NULL AND p.group_id = :groupId\n" +
                        "                  ORDER BY p.rating DESC),\n" +
                        "     PlayerRosterCount AS (SELECT r.player_id AS playerId,\n" +
                        "                                  COUNT(r.id) AS rosterCount\n" +
                        "                           FROM roster r\n" +
                        "                           WHERE r.group_id = :groupId\n" +
                        "                           GROUP BY r.player_id)\n" +
                        "SELECT ts.playerId,\n" +
                        "       ts.name,\n" +
                        "       ts.surname,\n" +
                        "       ts.rating,\n" +
                        "       COALESCE(prc.rosterCount, 0) AS rosterCount\n" +
                        "FROM TopRated ts\n" +
                        "         LEFT JOIN\n" +
                        "     PlayerRosterCount prc ON ts.playerId = prc.playerId\n" +
                        "ORDER BY ts.rating DESC", nativeQuery = true)
        List<Object[]> findTopRatedPlayers(@Param("groupId") Integer groupId);

        @Query(value = "WITH RecentGames AS ( " +
                        "    SELECT g.id " +
                        "    FROM game g " +
                        "    WHERE g.is_played = true AND g.group_id = :groupId " +
                        "    ORDER BY g.date_time DESC " +
                        "    LIMIT 2 " +
                        ") " +
                        "SELECT DISTINCT r.player_id " +
                        "FROM roster r " +
                        "WHERE r.game_id IN (SELECT id FROM RecentGames) AND r.group_id = :groupId", nativeQuery = true)
        List<Integer> findPlayersInRecentGames(@Param("groupId") Integer groupId);

        @Query(value = "SELECT * FROM (WITH Last10WeeksMatches AS (SELECT r.player_id, r.game_id, r.rating, g.date_time FROM roster r JOIN game g ON r.game_id = g.id WHERE g.date_time >= NOW() - INTERVAL '12 weeks' AND g.is_played AND g.is_voted AND g.group_id = :groupId AND r.group_id = :groupId), Last4Games AS (SELECT id FROM game WHERE game.is_voted AND game.group_id = :groupId ORDER BY date_time DESC LIMIT 4), PlayerLast5Matches AS (SELECT r.player_id, r.game_id, r.rating, g.date_time, ROW_NUMBER() OVER (PARTITION BY r.player_id ORDER BY g.date_time DESC) AS match_rank FROM Last10WeeksMatches r JOIN game g ON r.game_id = g.id), EligiblePlayers AS (SELECT player_id FROM PlayerLast5Matches GROUP BY player_id HAVING COUNT(*) >= 5), ActivePlayers AS (SELECT player_id FROM roster WHERE game_id IN (SELECT id FROM Last4Games) AND group_id = :groupId GROUP BY player_id HAVING COUNT(DISTINCT game_id) >= 2), RecentStats AS (SELECT player_id, MAX(CASE WHEN match_rank = 1 THEN rating END) AS last_match_rating, AVG(CASE WHEN match_rank BETWEEN 2 AND 4 THEN rating END) AS last3_avg_rating FROM PlayerLast5Matches GROUP BY player_id), FinalChanges AS (SELECT rs.player_id, rs.last_match_rating, rs.last3_avg_rating, CASE WHEN rs.last3_avg_rating IS NOT NULL AND rs.last3_avg_rating <> 0 THEN (rs.last_match_rating - rs.last3_avg_rating) / rs.last3_avg_rating * 100 ELSE NULL END AS rating_change FROM RecentStats rs) SELECT fc.player_id, p.name, p.surname, p.rating, fc.last_match_rating, fc.last3_avg_rating, fc.rating_change as avgRatingChange FROM FinalChanges fc JOIN EligiblePlayers ep ON fc.player_id = ep.player_id JOIN ActivePlayers ap ON fc.player_id = ap.player_id JOIN player p ON p.id = fc.player_id WHERE fc.rating_change IS NOT NULL AND p.group_id = :groupId ORDER BY fc.rating_change DESC LIMIT 5) aa ORDER BY aa.rating DESC", nativeQuery = true)
        List<TopListProjection> getTopFormPlayers(@Param("groupId") Integer groupId);

        @Query(value = "WITH ranked_pairs AS (\n" +
                        "    -- 1⃣ Oyuncu çiftlerini hesapla ve maç sayılarına göre sırala\n" +
                        "    SELECT r1.player_id AS player1,\n" +
                        "           r2.player_id AS player2,\n" +
                        "           COUNT(*) AS games_together\n" +
                        "    FROM roster r1\n" +
                        "    JOIN roster r2\n" +
                        "        ON r1.game_id = r2.game_id\n" +
                        "        AND r1.team_color = r2.team_color\n" +
                        "        AND r1.player_id < r2.player_id\n" +
                        "    WHERE r1.group_id = :groupId\n" +
                        "      AND r2.group_id = :groupId\n" +
                        "    GROUP BY player1, player2\n" +
                        "    ORDER BY games_together DESC\n" +
                        "),\n" +
                        "filtered_pairs AS (\n" +
                        "    SELECT *,\n" +
                        "           ROW_NUMBER() OVER (\n" +
                        "               PARTITION BY player1 ORDER BY games_together DESC\n" +
                        "           ) AS rn1,\n" +
                        "           ROW_NUMBER() OVER (\n" +
                        "               PARTITION BY player2 ORDER BY games_together DESC\n" +
                        "           ) AS rn2\n" +
                        "    FROM ranked_pairs\n" +
                        ")\n" +
                        "SELECT player1 as player1Id, player2 as player2Id, pp1.name as player1Name,  pp2.name as player2Name, games_together\n"
                        +
                        "FROM filtered_pairs\n" +
                        "join player pp1 on pp1.id = player1\n" +
                        "join player pp2 on pp2.id = player2\n" +
                        "WHERE pp1.group_id = :groupId\n" +
                        "  AND pp2.group_id = :groupId", nativeQuery = true)
        List<TopListProjection> getLegendaryDuos(@Param("groupId") Integer groupId);

        @Query(value = "WITH ranked_rivals AS (\n" +
                        "    SELECT r1.player_id AS player1,\n" +
                        "           r2.player_id AS player2,\n" +
                        "           COUNT(*)     AS games_against\n" +
                        "    FROM roster r1\n" +
                        "    JOIN roster r2\n" +
                        "        ON r1.game_id = r2.game_id\n" +
                        "        AND r1.team_color <> r2.team_color\n" +
                        "        AND r1.player_id < r2.player_id\n" +
                        "    WHERE r1.group_id = :groupId\n" +
                        "      AND r2.group_id = :groupId\n" +
                        "    GROUP BY player1, player2\n" +
                        "    ORDER BY games_against DESC\n" +
                        "),\n" +
                        "filtered_rivals AS (\n" +
                        "    SELECT *,\n" +
                        "           ROW_NUMBER() OVER (\n" +
                        "               PARTITION BY player1 ORDER BY games_against DESC\n" +
                        "           ) AS rn1,\n" +
                        "           ROW_NUMBER() OVER (\n" +
                        "               PARTITION BY player2 ORDER BY games_against DESC\n" +
                        "           ) AS rn2\n" +
                        "    FROM ranked_rivals\n" +
                        ")\n" +
                        "SELECT r.player1 AS player1Id,\n" +
                        "       r.player2 AS player2Id,\n" +
                        "       p1.name AS player1Name,\n" +
                        "       p2.name AS player2Name,\n" +
                        "       r.games_against as games_against\n" +
                        "FROM filtered_rivals r\n" +
                        "JOIN player p1 ON p1.id = r.player1\n" +
                        "JOIN player p2 ON p2.id = r.player2\n" +
                        "WHERE (r.rn1 = 1 OR r.rn2 = 1)\n" +
                        "  AND p1.group_id = :groupId\n" +
                        "  AND p2.group_id = :groupId\n" +
                        "ORDER BY r.games_against DESC", nativeQuery = true)
        List<TopListProjection> getRivalDuos(@Param("groupId") Integer groupId);

        @Query(value = "select r.rating as last5GameRating\n" +
                        "from roster r\n" +
                        "         left join player p on r.player_id = p.id\n" +
                        "         left join game g on r.game_id = g.id\n" +
                        "where p.id = :playerId\n" +
                        "  and r.rating != 0\n" +
                        "  and g.is_voted\n" +
                        "  AND p.group_id = :groupId\n" +
                        "  AND r.group_id = :groupId\n" +
                        "  AND g.group_id = :groupId\n" +
                        "order by r.id desc\n" +
                        "limit 5", nativeQuery = true)
        List<Double> getLast5MatchRatingByPlayerId(@Param("playerId") Integer playerId,
                        @Param("groupId") Integer groupId);

        List<Player> findByIdIn(List<Integer> ids);

}