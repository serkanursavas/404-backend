package com.squad.squad.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.squad.squad.dto.TopScorerProjection;
import com.squad.squad.entity.Goal;

@Repository
public interface GoalRepository extends SecureJpaRepository<Goal, Integer> {

    @Query(value = "WITH TopScorers AS (\n" +
            "    SELECT\n" +
            "        g.player_id AS playerId,\n" +
            "        p.name AS name,\n" +
            "        p.surname AS surname,\n" +
            "        COUNT(g.id) AS goalCount\n" +
            "    FROM\n" +
            "        goal g\n" +
            "    JOIN\n" +
            "        player p ON g.player_id = p.id\n" +
            "    WHERE g.group_id = :groupId\n" +
            "    GROUP BY\n" +
            "        g.player_id, p.name, p.surname\n" +
            "    ORDER BY\n" +
            "        goalCount DESC\n" +
            "    LIMIT 10\n" +
            "),\n" +
            "PlayerRosterCount AS (\n" +
            "    SELECT\n" +
            "        r.player_id AS playerId,\n" +
            "        COUNT(r.id) AS rosterCount\n" +
            "    FROM\n" +
            "        roster r\n" +
            "    WHERE r.group_id = :groupId\n" +
            "    GROUP BY\n" +
            "        r.player_id\n" +
            ")\n" +
            "SELECT\n" +
            "    ts.playerId,\n" +
            "    ts.name,\n" +
            "    ts.surname,\n" +
            "    ts.goalCount,\n" +
            "    COALESCE(prc.rosterCount, 0) AS rosterCount\n" +
            "FROM TopScorers ts\n" +
            "LEFT JOIN PlayerRosterCount prc ON ts.playerId = prc.playerId\n" +
            "ORDER BY ts.goalCount DESC", nativeQuery = true)
    List<TopScorerProjection> findTopScorersNative(@Param("groupId") Integer groupId);

    @Query("SELECT g FROM Goal g WHERE g.game.id = :gameId")
    List<Goal> findGoalsByGameId(@Param("gameId") Integer gameId);
}