package com.squad.squad.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.GamesDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.repository.GameRepository;

import jakarta.transaction.Transactional;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<GamesDTO> getAllGames() {
        return gameRepository.findAll().stream()
                .map(game -> new GamesDTO(game.getId(), game.getDateTime(), game.getHomeTeamScore(),
                        game.getAwayTeamScore()))
                .collect(Collectors.toList());
    }

    public Game getGameById(Integer id) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        return game;
    }

    @Transactional
    public Game saveGame(Game game) {

        return gameRepository.save(game);
    }

    public Game updateGame(Integer id, Game updatedGame) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (updatedGame.getLocation() != null) {
            game.setLocation(updatedGame.getLocation());
        }

        if (updatedGame.getWeather() != null) {
            game.setWeather(updatedGame.getWeather());
        }

        if (updatedGame.getDateTime() != null) {
            game.setDateTime(updatedGame.getDateTime());
        }

        if (updatedGame.getHomeTeamScore() != null) {
            game.setHomeTeamScore(updatedGame.getHomeTeamScore());
        }

        if (updatedGame.getAwayTeamScore() != null) {
            game.setAwayTeamScore(updatedGame.getAwayTeamScore());
        }

        return gameRepository.save(game);
    }

    @Transactional
    public void deleteGame(Integer id) {
        gameRepository.deleteById(id);
    }

}