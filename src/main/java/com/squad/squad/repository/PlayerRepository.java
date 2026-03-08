package com.squad.squad.repository;

import java.util.List;
import java.util.Optional;

import com.squad.squad.dto.TopListProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

    Optional<Player> findByIdAndSquadId(Integer id, Integer squadId);

    List<Player> findByActive(boolean active);

    List<Player> findByActiveAndSquadId(boolean active, Integer squadId);

    List<Player> findBySquadId(Integer squadId);

    Optional<Player> findByIdAndActive(Integer id, boolean active);

    @Query(value = "with TopRated as (SELECT p.id AS playerId, p.name, p.surname, p.rating\n" +
            "                  FROM player p\n" +
            "                  WHERE p.rating IS NOT NULL AND p.squad_id = :squadId\n" +
            "                  ORDER BY p.rating DESC),\n" +
            "     PlayerRosterCount AS (SELECT r.player_id AS playerId,\n" +
            "                                  COUNT(r.id) AS rosterCount\n" +
            "                           FROM roster r\n" +
            "                           JOIN game g ON r.game_id = g.id\n" +
            "                           WHERE g.squad_id = :squadId\n" +
            "                           GROUP BY r.player_id)\n" +
            "SELECT ts.playerId,\n" +
            "       ts.name,\n" +
            "       ts.surname,\n" +
            "       ts.rating,\n" +
            "       COALESCE(prc.rosterCount, 0) AS rosterCount\n" +
            "FROM TopRated ts\n" +
            "         LEFT JOIN\n" +
            "     PlayerRosterCount prc ON ts.playerId = prc.playerId\n" +
            "ORDER BY ts.rating DESC",
            nativeQuery = true)
    List<Object[]> findTopRatedPlayers(@Param("squadId") Integer squadId);

    @Query(value = "WITH RecentGames AS ( " +
            "    SELECT g.id " +
            "    FROM game g " +
            "    WHERE g.is_played = true AND g.squad_id = :squadId " +
            "    ORDER BY g.date_time DESC " +
            "    LIMIT 2 " +
            ") " +
            "SELECT DISTINCT r.player_id " +
            "FROM roster r " +
            "WHERE r.game_id IN (SELECT id FROM RecentGames)",
            nativeQuery = true)
    List<Integer> findPlayersInRecentGames(@Param("squadId") Integer squadId);

    @Query(value = "select *\n" +
            "from (WITH Last10WeeksMatches AS (SELECT r.player_id,\n" +
            "                                         r.game_id,\n" +
            "                                         r.rating,\n" +
            "                                         g.date_time\n" +
            "                                  FROM roster r\n" +
            "                                           JOIN game g ON r.game_id = g.id\n" +
            "                                  WHERE g.date_time >= NOW() - INTERVAL '12 weeks'\n" +
            "                                    and g.is_played and g.is_voted AND g.squad_id = :squadId),\n" +
            "\n" +
            "           Last4Games AS (SELECT id\n" +
            "                          FROM game\n" +
            "                           where game.is_voted AND game.squad_id = :squadId\n" +
            "                          ORDER BY date_time DESC\n" +
            "                          LIMIT 4),\n" +
            "\n" +
            "           PlayerLast5Matches AS (SELECT r.player_id,\n" +
            "                                         r.game_id,\n" +
            "                                         r.rating,\n" +
            "                                         g.date_time,\n" +
            "                                         ROW_NUMBER() OVER (PARTITION BY r.player_id ORDER BY g.date_time DESC) AS match_rank\n" +
            "                                  FROM Last10WeeksMatches r\n" +
            "                                           JOIN game g ON r.game_id = g.id),\n" +
            "\n" +
            "           EligiblePlayers AS (SELECT player_id\n" +
            "                               FROM PlayerLast5Matches\n" +
            "                               GROUP BY player_id\n" +
            "                               HAVING COUNT(*) >= 5),\n" +
            "\n" +
            "           ActivePlayers AS (SELECT player_id\n" +
            "                             FROM roster\n" +
            "                             WHERE game_id IN (SELECT id FROM Last4Games)\n" +
            "                             GROUP BY player_id\n" +
            "                             HAVING COUNT(DISTINCT game_id) >= 2),\n" +
            "\n" +
            "           RecentStats AS (SELECT player_id,\n" +
            "                                  MAX(CASE WHEN match_rank = 1 THEN rating END)             AS last_match_rating,\n" +
            "                                  AVG(CASE WHEN match_rank BETWEEN 2 AND 4 THEN rating END) AS last3_avg_rating\n" +
            "                           FROM PlayerLast5Matches\n" +
            "                           GROUP BY player_id),\n" +
            "\n" +
            "           FinalChanges AS (SELECT rs.player_id,\n" +
            "                                   rs.last_match_rating,\n" +
            "                                   rs.last3_avg_rating,\n" +
            "                                   CASE\n" +
            "                                       WHEN rs.last3_avg_rating IS NOT NULL AND rs.last3_avg_rating <> 0\n" +
            "                                           THEN (rs.last_match_rating - rs.last3_avg_rating) / rs.last3_avg_rating * 100\n" +
            "                                       ELSE NULL\n" +
            "                                       END AS rating_change\n" +
            "                            FROM RecentStats rs)\n" +
            "      SELECT fc.player_id,\n" +
            "             p.name,\n" +
            "             p.surname,\n" +
            "             p.rating,\n" +
            "             fc.last_match_rating,\n" +
            "             fc.last3_avg_rating,\n" +
            "             fc.rating_change as avgRatingChange\n" +
            "      FROM FinalChanges fc\n" +
            "               JOIN EligiblePlayers ep ON fc.player_id = ep.player_id\n" +
            "               JOIN ActivePlayers ap ON fc.player_id = ap.player_id\n" +
            "               JOIN player p ON p.id = fc.player_id\n" +
            "      WHERE fc.rating_change IS NOT NULL\n" +
            "      ORDER BY fc.rating_change DESC\n" +
            "      LIMIT 5) aa\n" +
            "order by aa.rating desc", nativeQuery = true)
    List<TopListProjection> getTopFormPlayers(@Param("squadId") Integer squadId);

    @Query(value = "WITH ranked_pairs AS (\n" +
            "    SELECT r1.player_id AS player1,\n" +
            "           r2.player_id AS player2,\n" +
            "           COUNT(*) AS games_together\n" +
            "    FROM roster r1\n" +
            "    JOIN roster r2\n" +
            "        ON r1.game_id = r2.game_id\n" +
            "        AND r1.team_color = r2.team_color\n" +
            "        AND r1.player_id < r2.player_id\n" +
            "    JOIN game g ON r1.game_id = g.id\n" +
            "    WHERE g.squad_id = :squadId\n" +
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
            "SELECT player1 as player1Id, player2 as player2Id, pp1.name as player1Name,  pp2.name as player2Name, games_together\n" +
            "FROM filtered_pairs\n" +
            "join player pp1 on pp1.id = player1\n" +
            "join player pp2 on pp2.id = player2", nativeQuery = true)
    List<TopListProjection> getLegendaryDuos(@Param("squadId") Integer squadId);

    @Query(value = "WITH ranked_rivals AS (\n" +
            "    SELECT r1.player_id AS player1,\n" +
            "           r2.player_id AS player2,\n" +
            "           COUNT(*)     AS games_against\n" +
            "    FROM roster r1\n" +
            "    JOIN roster r2\n" +
            "        ON r1.game_id = r2.game_id\n" +
            "        AND r1.team_color <> r2.team_color\n" +
            "        AND r1.player_id < r2.player_id\n" +
            "    JOIN game g ON r1.game_id = g.id\n" +
            "    WHERE g.squad_id = :squadId\n" +
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
            "WHERE r.rn1 = 1 OR r.rn2 = 1\n" +
            "ORDER BY r.games_against DESC", nativeQuery = true)
    List<TopListProjection> getRivalDuos(@Param("squadId") Integer squadId);

    @Query(value = "select r.rating as last5GameRating\n" +
            "from roster r\n" +
            "         left join player p on r.player_id = p.id\n" +
            "         left join game g on r.game_id = g.id\n" +
            "where p.id = :playerId\n" +
            "  and r.rating != 0\n" +
            "and g.is_voted\n" +
            "and g.squad_id = :squadId\n" +
            "order by r.id desc\n" +
            "limit 5", nativeQuery = true)
    List<Double> getLast5MatchRatingByPlayerId(@Param("playerId") Integer playerId, @Param("squadId") Integer squadId);

    List<Player> findByIdIn(List<Integer> ids);
}
