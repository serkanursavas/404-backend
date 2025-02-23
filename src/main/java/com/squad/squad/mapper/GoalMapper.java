package com.squad.squad.mapper;

import com.squad.squad.dto.goal.GoalResponseDTO;
import com.squad.squad.dto.roster.RosterResponseDTO;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Roster;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalMapper {

    GoalResponseDTO goalToGoalResponseDTO(Goal goal);

    List<GoalResponseDTO> goalsToGoalResponseDTOs(List<Goal> goals);

    @AfterMapping
    default void setAdditionalFields(@MappingTarget GoalResponseDTO goalResponseDTO, Goal goal) {
        if (goal.getPlayer() != null) {
            goalResponseDTO.setPlayerName(goal.getPlayer().getName() + " " + goal.getPlayer().getSurname());
            goalResponseDTO.setPlayerId(goal.getPlayer().getId());
        }
    }
}