package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Goal;

public interface GoalService {
    List<Goal> getAllGoals();

    List<GoalDTO> getGoalsByGameId(Integer gameId);

    void addGoals(List<GoalDTO> goals);
}
