package com.squad.squad.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Goal;
import com.squad.squad.repository.GoalRepository;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    public List<GoalDTO> getGoalsByGameId(Integer game_id) {
        return goalRepository.findGoalsByGameId(game_id).stream().map(
                goal -> new GoalDTO(game_id, goal.getPlayer().getId(), goal.getPlayer().getName(), goal.getTeamColor()))
                .collect(Collectors.toList());
    }

    public Goal addGoal(Goal goal) {
        return goalRepository.save(goal);
    }

}
