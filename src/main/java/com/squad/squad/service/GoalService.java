package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.GoalDTO;

public interface GoalService {
    List<GoalDTO> getAllGoals();

    List<GoalDTO> getGoalsByGameId(Integer gameId);

    void addGoals(List<GoalDTO> goals);
}
