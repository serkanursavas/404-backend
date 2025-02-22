package com.squad.squad.mapper;

import java.util.List;

import com.squad.squad.dto.player.GetAllActivePlayersDTO;
import com.squad.squad.dto.player.GetAllPlayersDTO;
import com.squad.squad.dto.player.PlayerUpdateRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.entity.Player;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
    PlayerMapper INSTANCE = Mappers.getMapper(PlayerMapper.class);

    PlayerDTO playerToPlayerDTO(Player player);

    @Mapping(source = "active", target = "active")
    List<PlayerDTO> playersToPlayerDTOs(List<Player> players);

    PlayerUpdateRequestDTO playerToPlayerUpdateRequestDTO(Player player);

    Player playerDTOToPlayer(PlayerDTO playerDTO);

    @Mapping(source = "active", target = "active")
    List<GetAllPlayersDTO> playersToGetAllPlayersDTOs(List<Player> players);

    @Mapping(source = "name", target = "playerName")
    GetAllActivePlayersDTO playerToGetAllActivePlayersDTO(Player player);

    List<GetAllActivePlayersDTO> playersToGetAllActivePlayersDTOs(List<Player> players);
}