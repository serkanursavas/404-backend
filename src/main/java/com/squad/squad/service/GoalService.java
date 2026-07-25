package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.dto.TopListsDTO;
import com.squad.squad.dto.goal.AddGoalsRequestDTO;
import com.squad.squad.dto.goal.GoalAddRequestDTO;

public interface GoalService {
    List<GoalDTO> getAllGoals();

    List<GoalDTO> getGoalsByGameId(Integer gameId);

    void addGoals(AddGoalsRequestDTO requestDto);

    // Bir maçın gollerini "nihai liste" olarak senkronize eder: eksikse ekler, fazlaysa
    // en yenisinden soft-delete eder, dokunulmayanları korur. isPlayed'a dokunmaz.
    void updateGoalsForGame(Integer gameId, List<GoalAddRequestDTO> desiredGoals);

    List<TopListsDTO> getTopScorers();
}