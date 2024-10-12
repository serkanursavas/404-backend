package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.squad.squad.dto.goal.GoalAddRequestDTO;
import com.squad.squad.entity.Roster;
import org.springframework.stereotype.Component;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.enums.TeamColor;

@Component
public class GoalDTOValidator {

    public List<String> validate(List<GoalAddRequestDTO> goals, List<Roster> gameRosters) {
        List<String> errors = new ArrayList<>();

        if (goals == null || goals.isEmpty()) {
            errors.add("Goal list cannot be null or empty");
            return errors;
        }

        Map<Integer, String> playerTeamMap = gameRosters.stream()
                .collect(Collectors.toMap(roster -> roster.getPlayer().getId(), Roster::getTeamColor));

        for (GoalAddRequestDTO goal : goals) {

            if (goal.getPlayerId() == null) {
                errors.add("Player ID cannot be null");
                continue;
            }

            if (goal.getTeamColor() == null || goal.getTeamColor().isEmpty()) {
                errors.add("Team color cannot be empty for player ID " + goal.getPlayerId());
                continue;
            }

            try {
                TeamColor goalTeamColor = TeamColor.fromString(goal.getTeamColor());
                String playerTeamColor = playerTeamMap.get(goal.getPlayerId());

                if (playerTeamColor == null) {
                    errors.add("Player ID " + goal.getPlayerId() + " is not in the roster for this game.");
                } else if (!playerTeamColor.equalsIgnoreCase(goalTeamColor.toString())) {
                    errors.add("Player ID " + goal.getPlayerId() + " cannot score for the opposite team.");
                }
            } catch (Exception e) {
                errors.add("Invalid team color: " + goal.getTeamColor() + " for player ID " + goal.getPlayerId());
            }
        }

        return errors;
    }
}