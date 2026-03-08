package com.squad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Goal;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {

    List<Goal> findGoalsByGameId(Integer game_id);

    @Query("SELECT g FROM Goal g WHERE g.game.squad.id = :squadId")
    List<Goal> findAllBySquadId(@Param("squadId") Integer squadId);

    @Query(value = "WITH TopScorers AS (\n" +
            "    SELECT \n" +
            "        gl.player_id AS playerId, \n" +
            "        p.name AS name, \n" +
            "        p.surname AS surname, \n" +
            "        COUNT(gl.id) AS goalCount\n" +
            "    FROM \n" +
            "        goal gl\n" +
            "    JOIN \n" +
            "        player p ON gl.player_id = p.id\n" +
            "    JOIN \n" +
            "        game g ON gl.game_id = g.id\n" +
            "    WHERE g.squad_id = :squadId\n" +
            "    GROUP BY \n" +
            "        gl.player_id, p.name, p.surname\n" +
            "    ORDER BY \n" +
            "        goalCount DESC\n" +
            "    LIMIT 10\n" +
            "),\n" +
            "PlayerRosterCount AS (\n" +
            "    SELECT \n" +
            "        r.player_id AS playerId,\n" +
            "        COUNT(r.id) AS rosterCount\n" +
            "    FROM\n" +
            "        roster r\n" +
            "    JOIN game g ON r.game_id = g.id\n" +
            "    WHERE g.squad_id = :squadId\n" +
            "    GROUP BY\n" +
            "        r.player_id\n" +
            ")\n" +
            "SELECT \n" +
            "    ts.playerId, \n" +
            "    ts.name, \n" +
            "    ts.surname, \n" +
            "    ts.goalCount, \n" +
            "    COALESCE(prc.rosterCount, 0) AS rosterCount\n" +
            "FROM \n" +
            "    TopScorers ts\n" +
            "LEFT JOIN \n" +
            "    PlayerRosterCount prc ON ts.playerId = prc.playerId\n" +
            "ORDER BY \n" +
            "    ts.goalCount DESC;", nativeQuery = true)
    List<Object[]> findTopScorersNative(@Param("squadId") Integer squadId);
}
