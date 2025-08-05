package com.squad.squad.mapper;

import com.squad.squad.dto.GameLocationDTO;
import com.squad.squad.entity.GameLocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameLocationMapper {

    public GameLocationDTO gameLocationToGameLocationDTO(GameLocation gameLocation) {
        if (gameLocation == null) {
            return null;
        }

        GameLocationDTO gameLocationDTO = new GameLocationDTO();
        gameLocationDTO.setId(gameLocation.getId());
        gameLocationDTO.setLocation(gameLocation.getLocation());
        gameLocationDTO.setAddress(gameLocation.getAddress());

        return gameLocationDTO;
    }

    public GameLocation gameLocationDTOToGameLocation(GameLocationDTO gameLocationDTO) {
        if (gameLocationDTO == null) {
            return null;
        }

        GameLocation gameLocation = new GameLocation();
        gameLocation.setId(gameLocationDTO.getId());
        gameLocation.setLocation(gameLocationDTO.getLocation());
        gameLocation.setAddress(gameLocationDTO.getAddress());

        return gameLocation;
    }

    public List<GameLocationDTO> gameLocationListToGameLocationDTOList(List<GameLocation> gameLocations) {
        if (gameLocations == null) {
            return null;
        }

        return gameLocations.stream()
                .map(this::gameLocationToGameLocationDTO)
                .collect(Collectors.toList());
    }

    public List<GameLocation> gameLocationDTOListToGameLocationList(List<GameLocationDTO> gameLocationDTOs) {
        if (gameLocationDTOs == null) {
            return null;
        }

        return gameLocationDTOs.stream()
                .map(this::gameLocationDTOToGameLocation)
                .collect(Collectors.toList());
    }
}