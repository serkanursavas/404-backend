package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.squad.squad.dto.roster.RosterCreateDTO;
import com.squad.squad.dto.roster.RosterUpdateDTO;
import com.squad.squad.enums.TeamColor;
import org.springframework.stereotype.Component;

import com.squad.squad.dto.RosterDTO;

@Component
public class RosterDTOValidator {

    public <T extends RosterDTO> List<String> validate(List<T> rosters) {
        List<String> errors = new ArrayList<>();
        Set<Integer> playerIds = new HashSet<>();
        int whiteTeamCount = 0;
        int blackTeamCount = 0;

        for (T roster : rosters) {
            // Duplicate player ID kontrolü
            if (!playerIds.add(roster.getPlayerId())) {
                errors.add("A player cannot be in the same game roster more than once. Duplicate player id: "
                        + roster.getPlayerId());
            }

            // teamColor kontrolü
            if (roster.getTeamColor() != null) {
                try {
                    TeamColor teamColor = TeamColor.fromString(roster.getTeamColor());
                    if (teamColor == TeamColor.WHITE) {
                        whiteTeamCount++;
                    } else if (teamColor == TeamColor.BLACK) {
                        blackTeamCount++;
                    }
                } catch (RuntimeException e) {
                    errors.add("Invalid team color for player ID " + roster.getPlayerId() + ": " + roster.getTeamColor());
                }
            } else {
                errors.add("Team color cannot be null for player ID " + roster.getPlayerId());
            }
        }

        // Takım renklerinin sayısını kontrol et
        if (whiteTeamCount != blackTeamCount) {
            errors.add("Teams must have an equal number of players. White team has " + whiteTeamCount +
                    " players, while Black team has " + blackTeamCount + " players.");
        }

        return errors;
    }
}