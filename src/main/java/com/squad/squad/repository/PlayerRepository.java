package com.squad.squad.repository;

import java.util.List;
import java.util.Optional;

import com.squad.squad.dto.PlayerProjection;
import com.squad.squad.dto.TopListProjection;
import com.squad.squad.dto.TopListsDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

    List<Player> findByActive(boolean active);

    Optional<Player> findByIdAndActive(Integer id, boolean active);

    @Query(value = "with TopRated as (SELECT p.id AS playerId, p.name, p.surname, p.rating\n" +
            "                  FROM player p\n" +
            "                  WHERE p.rating IS NOT NULL\n" +
            "                  ORDER BY p.rating DESC),\n" +
            "     PlayerRosterCount AS (SELECT r.player_id AS playerId,\n" +
            "                                  COUNT(r.id) AS rosterCount\n" +
            "                           FROM roster r\n" +
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
    List<Object[]> findTopRatedPlayers();

    @Query(value = "WITH RecentGames AS ( " +
            "    SELECT g.id " +
            "    FROM game g " +
            "    WHERE g.is_played = true " +
            "    ORDER BY g.date_time DESC " +
            "    LIMIT 2 " +
            ") " +
            "SELECT DISTINCT r.player_id " +
            "FROM roster r " +
            "WHERE r.game_id IN (SELECT id FROM RecentGames)",
            nativeQuery = true)
    List<Integer> findPlayersInRecentGames();


    @Query(value = "select *\n" +
            "from (WITH Last10WeeksMatches AS (SELECT r.player_id,\n" +
            "                                         r.game_id,\n" +
            "                                         r.rating,\n" +
            "                                         g.date_time\n" +
            "                                  FROM roster r\n" +
            "                                           JOIN game g ON r.game_id = g.id\n" +
            "                                  WHERE g.date_time >= NOW() - INTERVAL '12 weeks'\n" +
            "                                    and g.is_played and g.is_voted),\n" +
            "\n" +
            "           -- Son 3 maçı belirle\n" +
            "           Last4Games AS (SELECT id\n" +
            "                          FROM game" +
            "                           where game.is_voted\n" +
            "                          ORDER BY date_time DESC\n" +
            "                          LIMIT 4),\n" +
            "\n" +
            "           -- Son 5 maçı sıralıyoruz\n" +
            "           PlayerLast5Matches AS (SELECT r.player_id,\n" +
            "                                         r.game_id,\n" +
            "                                         r.rating,\n" +
            "                                         g.date_time,\n" +
            "                                         ROW_NUMBER() OVER (PARTITION BY r.player_id ORDER BY g.date_time DESC) AS match_rank\n" +
            "                                  FROM Last10WeeksMatches r\n" +
            "                                           JOIN game g ON r.game_id = g.id),\n" +
            "\n" +
            "           -- En az 5 maç oynamış oyuncular\n" +
            "           EligiblePlayers AS (SELECT player_id\n" +
            "                               FROM PlayerLast5Matches\n" +
            "                               GROUP BY player_id\n" +
            "                               HAVING COUNT(*) >= 5),\n" +
            "\n" +
            "           -- Son 3 maçtan en az 2’sinde aktif olan oyuncular\n" +
            "           ActivePlayers AS (SELECT player_id\n" +
            "                             FROM roster\n" +
            "                             WHERE game_id IN (SELECT id FROM Last4Games)\n" +
            "                             GROUP BY player_id\n" +
            "                             HAVING COUNT(DISTINCT game_id) >= 2),\n" +
            "\n" +
            "           -- Son maç ve önceki 3 maçın ortalamasını hesaplıyoruz\n" +
            "           RecentStats AS (SELECT player_id,\n" +
            "                                  MAX(CASE WHEN match_rank = 1 THEN rating END)             AS last_match_rating,\n" +
            "                                  AVG(CASE WHEN match_rank BETWEEN 2 AND 4 THEN rating END) AS last3_avg_rating\n" +
            "                           FROM PlayerLast5Matches\n" +
            "                           GROUP BY player_id),\n" +
            "\n" +
            "           -- Rating değişimini hesaplıyoruz\n" +
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
    List<TopListProjection> getTopFormPlayers();

    @Query(value = "WITH ranked_pairs AS (\n" +
            "    -- 1\uFE0F⃣ Oyuncu çiftlerini hesapla ve maç sayılarına göre sırala\n" +
            "    SELECT r1.player_id AS player1,\n" +
            "           r2.player_id AS player2,\n" +
            "           COUNT(*) AS games_together\n" +
            "    FROM roster r1\n" +
            "    JOIN roster r2\n" +
            "        ON r1.game_id = r2.game_id\n" +
            "        AND r1.team_color = r2.team_color\n" +
            "        AND r1.player_id < r2.player_id\n" +
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
    List<TopListProjection> getLegendaryDuos();

    @Query(value = "WITH ranked_rivals AS (\n" +
            "    SELECT r1.player_id AS player1,\n" +
            "           r2.player_id AS player2,\n" +
            "           COUNT(*)     AS games_against\n" +
            "    FROM roster r1\n" +
            "    JOIN roster r2\n" +
            "        ON r1.game_id = r2.game_id\n" +
            "        AND r1.team_color <> r2.team_color\n" +
            "        AND r1.player_id < r2.player_id\n" +
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
    List<TopListProjection> getRivalDuos();

    @Query(value = "select r.rating as last5GameRating\n" +
            "from roster r\n" +
            "         left join player p on r.player_id = p.id\n" +
            "         left join game g on r.game_id = g.id\n" +
            "where p.id = :playerId\n" +
            "  and r.rating != 0\n" +
            "and g.is_voted\n" +
            "order by r.id desc\n" +
            "limit 5", nativeQuery = true)
    List<Double> getLast5MatchRatingByPlayerId(Integer playerId);

    List<Player> findByIdIn(List<Integer> ids);



}