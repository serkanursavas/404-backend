package com.squad.squad.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.GamesDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Roster;
import com.squad.squad.repository.GameRepository;
import com.squad.squad.repository.RatingRepository;

import jakarta.transaction.Transactional;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final RosterService rosterService;
    private final RatingRepository ratingRepository;

    public GameService(GameRepository gameRepository, RosterService rosterService, RatingRepository ratingRepository) {
        this.gameRepository = gameRepository;
        this.rosterService = rosterService;
        this.ratingRepository = ratingRepository;
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
    public Game createGameWithRoster(Game game, List<Roster> rosters) {

        Game savedGame = gameRepository.save(game);
        for (Roster roster : rosters) {
            roster.setGame(savedGame);
            rosterService.saveRoster(roster);
        }

        return savedGame;
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

        rosterService.deleteByGameId(id);

        gameRepository.deleteById(id);
    }

    public void checkIfVotingIsComplete(Integer game_id, String team_color) {

        Integer totalVotesATeam = ratingRepository.countByRosterGameIdAndTeamColor(game_id, team_color);

        Game game = getGameById(game_id);
        Integer expectedVotesForATeam = (game.getRoster().size() / 2) * ((game.getRoster().size() / 2) - 1);

        if (totalVotesATeam == expectedVotesForATeam) {
            rosterService.updateRatingsForGame(game_id, team_color);
        }
    }

}
