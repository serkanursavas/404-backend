package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.dto.goal.GoalAddRequestDTO;

public interface GoalService {
    List<GoalDTO> getAllGoals();

    List<GoalDTO> getGoalsByGameId(Integer gameId);

    void addGoals(List<GoalAddRequestDTO> goals);
}