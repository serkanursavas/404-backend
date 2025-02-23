package com.squad.squad.mapper;

import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.dto.game.GameResponseDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.dto.game.NextGameResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.entity.Game;

@Mapper(uses = {GameLocationMapper.class}, componentModel = "spring")
public interface GameMapper {


    GameDTO gameToGameDTO(Game game);

    GameResponseDTO gameToGameResponseDTO(Game game);

    GameUpdateRequestDTO gameToGameUpdateRequestDTO(Game game);

    Game gameDTOToGame(GameDTO gameDto);

    NextGameResponseDTO gameToNextGameResponseDTO(Game game);
}