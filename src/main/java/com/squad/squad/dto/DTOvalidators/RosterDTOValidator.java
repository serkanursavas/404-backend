package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.squad.squad.dto.RosterDTO;

@Component
public class RosterDTOValidator {

    public List<String> validate(List<RosterDTO> rosters) {
        List<String> errors = new ArrayList<>();
        Set<Integer> playerIds = new HashSet<>();

        for (RosterDTO roster : rosters) {
            if (!playerIds.add(roster.getPlayerId())) {
                errors.add("A player cannot be in the same game roster more than once. Duplicate player id: "
                        + roster.getPlayerId());
            }
        }

        return errors;
    }

}
