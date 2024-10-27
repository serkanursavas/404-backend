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

    @Query("SELECT new com.squad.squad.dto.TopListsDTO(g.player.id, g.player.name, g.player.surname, COUNT(g)) " +
            "FROM Goal g " +
            "GROUP BY g.player.id, g.player.name, g.player.surname " +
            "ORDER BY COUNT(g) DESC")
    List<TopListsDTO> findTopScorers();
}