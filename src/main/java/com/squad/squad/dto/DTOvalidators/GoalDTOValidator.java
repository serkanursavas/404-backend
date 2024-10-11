package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.List;

import com.squad.squad.dto.goal.GoalAddRequestDTO;
import org.springframework.stereotype.Component;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.enums.TeamColor;

@Component
public class GoalDTOValidator {

    public List<String> validate(List<GoalAddRequestDTO> goals) {

        List<String> errors = new ArrayList<>();

        if (goals == null || goals.isEmpty()) {
            errors.add("Goal list cannot be null or empty");
            return errors;
        }

        for (GoalAddRequestDTO goal : goals) {

            if (goal.getGameId() == null) {
                errors.add("Game ID cannot be null");
            }

            if (goal.getPlayerId() == null) {
                errors.add("Player ID cannot be null");
            }

            if (goal.getTeamColor() == null || goal.getTeamColor().isEmpty()) {
                errors.add("Team color cannot be empty");
            } else {
                try {
                    TeamColor.fromString(goal.getTeamColor());
                } catch (Exception e) {
                    errors.add(e.getMessage());
                }
            }
        }

        return errors;
    }
}