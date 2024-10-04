package com.squad.squad.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.entity.Player;

@Mapper
public interface PlayerMapper {
    PlayerMapper INSTANCE = Mappers.getMapper(PlayerMapper.class);

    PlayerDTO playerToPlayerDTO(Player player);

    Player playerDTOToPlayer(PlayerDTO playerDTO);

    List<PlayerDTO> playersToPlayerDTOs(List<Player> players);

    List<Player> playerDTOsToPlayers(List<PlayerDTO> playerDTOs);
}
