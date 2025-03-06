package com.squad.squad.mapper;

import com.squad.squad.dto.GameLocationDTO;
import com.squad.squad.entity.GameLocation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GameLocationMapper {

    GameLocationDTO gameLocationToGameLocationDTO(GameLocation gameLocation);

    GameLocation gameLocationDTOToGameLocation(GameLocationDTO gameLocationDTO);

    List<GameLocationDTO> gameLocationListToGameLocationDTOList(List<GameLocation> gameLocations);

    List<GameLocation> gameLocationDTOListToGameLocationList(List<GameLocationDTO> gameLocationDTOs);

}