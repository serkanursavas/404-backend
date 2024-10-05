package com.squad.squad.dto.mapper;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Roster;
import org.springframework.stereotype.Component;

@Component
public class RosterDTOMapper {

    public RosterDTO mapper(Roster roster) {
        if (roster == null) {
            return null;
        }

        RosterDTO dto = new RosterDTO();
        dto.setId(roster.getId());
        dto.setTeamColor(roster.getTeamColor());
        dto.setPlayerId(roster.getPlayer() != null ? roster.getPlayer().getId() : null);
        dto.setRating(roster.getRating());
        dto.setPlayerName(roster.getPlayer() != null ? roster.getPlayer().getName() : null);
        return dto;
    }
}