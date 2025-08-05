package com.squad.squad.mapper;

import com.squad.squad.dto.goal.GoalResponseDTO;
import com.squad.squad.entity.Goal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoalMapper {

    public GoalResponseDTO goalToGoalResponseDTO(Goal goal) {
        if (goal == null) {
            return null;
        }

        GoalResponseDTO goalResponseDTO = new GoalResponseDTO();
        goalResponseDTO.setTeamColor(goal.getTeamColor());

        // Set additional fields
        setAdditionalFields(goalResponseDTO, goal);

        return goalResponseDTO;
    }

    public List<GoalResponseDTO> goalsToGoalResponseDTOs(List<Goal> goals) {
        if (goals == null) {
            return null;
        }

        return goals.stream()
                .map(this::goalToGoalResponseDTO)
                .collect(Collectors.toList());
    }

    private void setAdditionalFields(GoalResponseDTO goalResponseDTO, Goal goal) {
        if (goal.getPlayer() != null) {
            goalResponseDTO.setPlayerName(goal.getPlayer().getName() + " " + goal.getPlayer().getSurname());
            goalResponseDTO.setPlayerId(goal.getPlayer().getId());
        }
    }
}