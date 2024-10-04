package com.squad.squad.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Roster;

@Mapper
public interface RosterMapper {

    RosterMapper INSTANCE = Mappers.getMapper(RosterMapper.class);

    RosterDTO rosterToRosterDTO(Roster roster);

    Roster rosterDTOToRoster(RosterDTO rosterDTO);

    List<RosterDTO> rostersToRostersDTO(List<Roster> rosters);

    List<Roster> rostersDTOToRosters(List<RosterDTO> rosters);

}