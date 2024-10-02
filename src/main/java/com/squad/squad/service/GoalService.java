package com.squad.squad.service;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Goal;

import java.util.List;

public interface GoalService {
    List<Goal> getAllGoals();

    List<GoalDTO> getGoalsByGameId(Integer game_id);

    void addGoals(List<GoalDTO> goalDtos);
}
