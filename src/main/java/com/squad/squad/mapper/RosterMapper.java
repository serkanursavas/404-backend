package com.squad.squad.mapper;

import java.util.List;

import com.squad.squad.dto.roster.RosterResponseDTO;
import com.squad.squad.dto.roster.RosterUpdateDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.repository.GameRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Roster;

@Mapper(componentModel = "spring")
public interface RosterMapper {


    RosterDTO rosterToRosterDTO(Roster roster);

    Roster rosterDTOToRoster(RosterDTO rosterDTO);

    List<RosterDTO> rostersToRostersDTO(List<Roster> rosters);

    List<Roster> rosterUpdateDTOsToRosters(List<RosterUpdateDTO> rosters);

    List<RosterResponseDTO> rostersToRosterResponseDTOs(List<Roster> rosters);

    RosterResponseDTO rosterToRosterResponseDTO(Roster roster);

    @AfterMapping
    default void setAdditionalFields(@MappingTarget RosterResponseDTO rosterResponseDTO, Roster roster) {
        if (roster.getPlayer() != null) {
            rosterResponseDTO.setPlayerName(roster.getPlayer().getName() + " " + roster.getPlayer().getSurname());
            rosterResponseDTO.setPlayerId(roster.getPlayer().getId());
        }
    }
}