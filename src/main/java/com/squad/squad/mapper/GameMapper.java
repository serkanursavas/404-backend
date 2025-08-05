package com.squad.squad.mapper;

import org.springframework.stereotype.Component;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.GameLocationDTO;
import com.squad.squad.dto.game.GameResponseDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.dto.game.NextGameResponseDTO;
import com.squad.squad.entity.Game;

@Component
public class GameMapper {

    public GameDTO gameToGameDTO(Game game) {
        if (game == null) {
            return null;
        }

        GameDTO gameDTO = new GameDTO();
        gameDTO.setId(game.getId());
        gameDTO.setDateTime(game.getDateTime());
        gameDTO.setHomeTeamScore(game.getHomeTeamScore());
        gameDTO.setAwayTeamScore(game.getAwayTeamScore());
        gameDTO.setPlayed(game.isPlayed());
        gameDTO.setVoteMode(game.isVoted());

        return gameDTO;
    }

    public GameResponseDTO gameToGameResponseDTO(Game game) {
        if (game == null) {
            return null;
        }

        GameResponseDTO gameResponseDTO = new GameResponseDTO();
        gameResponseDTO.setId(game.getId());
        gameResponseDTO.setDateTime(game.getDateTime());
        gameResponseDTO.setHomeTeamScore(game.getHomeTeamScore());
        gameResponseDTO.setAwayTeamScore(game.getAwayTeamScore());
        gameResponseDTO.setPlayed(game.isPlayed());
        gameResponseDTO.setVoted(game.isVoted());
        gameResponseDTO.setLocation(game.getLocation());
        gameResponseDTO.setWeather(game.getWeather());
        gameResponseDTO.setMvpId(game.getMvpId());

        // GameLocation mapping
        if (game.getGameLocation() != null) {
            GameLocationDTO gameLocationDTO = new GameLocationDTO();
            gameLocationDTO.setId(game.getGameLocation().getId());
            gameLocationDTO.setLocation(game.getGameLocation().getLocation());
            gameLocationDTO.setAddress(game.getGameLocation().getAddress());
            gameLocationDTO.setLatitude(game.getGameLocation().getLatitude());
            gameLocationDTO.setLongitude(game.getGameLocation().getLongitude());
            gameResponseDTO.setGameLocation(gameLocationDTO);
        }

        return gameResponseDTO;
    }

    public GameUpdateRequestDTO gameToGameUpdateRequestDTO(Game game) {
        if (game == null) {
            return null;
        }

        GameUpdateRequestDTO gameUpdateRequestDTO = new GameUpdateRequestDTO();
        gameUpdateRequestDTO.setId(game.getId());
        gameUpdateRequestDTO.setDateTime(game.getDateTime());

        return gameUpdateRequestDTO;
    }

    public Game gameDTOToGame(GameDTO gameDto) {
        if (gameDto == null) {
            return null;
        }

        Game game = new Game();
        game.setId(gameDto.getId());
        game.setDateTime(gameDto.getDateTime());
        game.setHomeTeamScore(gameDto.getHomeTeamScore());
        game.setAwayTeamScore(gameDto.getAwayTeamScore());
        game.setPlayed(gameDto.isPlayed());
        game.setVoted(gameDto.isVoteMode());

        return game;
    }

    public NextGameResponseDTO gameToNextGameResponseDTO(Game game) {
        if (game == null) {
            return null;
        }

        NextGameResponseDTO nextGameResponseDTO = new NextGameResponseDTO();
        nextGameResponseDTO.setId(game.getId());
        nextGameResponseDTO.setDateTime(game.getDateTime());
        nextGameResponseDTO.setHomeTeamScore(game.getHomeTeamScore());
        nextGameResponseDTO.setAwayTeamScore(game.getAwayTeamScore());
        nextGameResponseDTO.setPlayed(game.isPlayed());
        nextGameResponseDTO.setVoted(game.isVoted());

        return nextGameResponseDTO;
    }
}