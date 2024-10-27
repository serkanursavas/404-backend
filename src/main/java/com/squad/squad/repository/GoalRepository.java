package com.squad.squad.repository;

import com.squad.squad.dto.TopListsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Goal;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {

    List<Goal> findGoalsByGameId(Integer game_id);

    @Query(value = "SELECT g.player_id AS playerId, p.name AS name, p.surname AS surname, COUNT(g.id) AS goalCount " +
            "FROM goal g " +
            "JOIN player p ON g.player_id = p.id " +
            "GROUP BY g.player_id, p.name, p.surname " +
            "ORDER BY goalCount DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findTopScorersNative();
}