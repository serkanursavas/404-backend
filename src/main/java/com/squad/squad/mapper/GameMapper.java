package com.squad.squad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.entity.Game;

@Mapper
public interface GameMapper {

    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    GameDTO gameToGameDTO(Game game);

    Game gameDTOToGame(GameDTO gameDto);

}
