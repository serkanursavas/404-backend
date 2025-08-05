package com.squad.squad.mapper;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.dto.roster.RosterResponseDTO;
import com.squad.squad.dto.roster.RosterUpdateDTO;
import com.squad.squad.entity.Roster;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RosterMapper {

    public RosterDTO rosterToRosterDTO(Roster roster) {
        if (roster == null) {
            return null;
        }

        RosterDTO rosterDTO = new RosterDTO();
        rosterDTO.setId(roster.getId());
        rosterDTO.setRating(roster.getRating());
        rosterDTO.setTeamColor(roster.getTeamColor());
        rosterDTO.setGameId(roster.getGame().getId());
        rosterDTO.setPlayerId(roster.getPlayer().getId());

        return rosterDTO;
    }

    public Roster rosterDTOToRoster(RosterDTO rosterDTO) {
        if (rosterDTO == null) {
            return null;
        }

        Roster roster = new Roster();
        roster.setId(rosterDTO.getId());
        roster.setRating(rosterDTO.getRating());
        roster.setTeamColor(rosterDTO.getTeamColor());

        return roster;
    }

    public List<RosterDTO> rostersToRostersDTO(List<Roster> rosters) {
        if (rosters == null) {
            return null;
        }

        return rosters.stream()
                .map(this::rosterToRosterDTO)
                .collect(Collectors.toList());
    }

    public List<Roster> rosterUpdateDTOsToRosters(List<RosterUpdateDTO> rosters) {
        if (rosters == null) {
            return null;
        }

        return rosters.stream()
                .map(this::rosterUpdateDTOToRoster)
                .collect(Collectors.toList());
    }

    private Roster rosterUpdateDTOToRoster(RosterUpdateDTO rosterUpdateDTO) {
        if (rosterUpdateDTO == null) {
            return null;
        }

        Roster roster = new Roster();
        roster.setId(rosterUpdateDTO.getId());
        roster.setTeamColor(rosterUpdateDTO.getTeamColor());

        return roster;
    }

    public List<RosterResponseDTO> rostersToRosterResponseDTOs(List<Roster> rosters) {
        if (rosters == null) {
            return null;
        }

        return rosters.stream()
                .map(this::rosterToRosterResponseDTO)
                .collect(Collectors.toList());
    }

    public RosterResponseDTO rosterToRosterResponseDTO(Roster roster) {
        if (roster == null) {
            return null;
        }

        RosterResponseDTO rosterResponseDTO = new RosterResponseDTO();
        rosterResponseDTO.setId(roster.getId());
        rosterResponseDTO.setRating(roster.getRating());
        rosterResponseDTO.setTeamColor(roster.getTeamColor());

        // Set additional fields
        setAdditionalFields(rosterResponseDTO, roster);

        return rosterResponseDTO;
    }

    private void setAdditionalFields(RosterResponseDTO rosterResponseDTO, Roster roster) {
        if (roster.getPlayer() != null) {
            rosterResponseDTO.setPlayerName(roster.getPlayer().getName() + " " + roster.getPlayer().getSurname());
            rosterResponseDTO.setPlayerId(roster.getPlayer().getId());
        }
    }
}