package com.squad.squad.service.impl;

import com.squad.squad.dto.GamesDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Roster;
import com.squad.squad.repository.GameRepository;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.service.GameService;
import com.squad.squad.service.RosterService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RosterService rosterService;
    private final RatingRepository ratingRepository;

    public GameServiceImpl(GameRepository gameRepository, RosterService rosterService, RatingRepository ratingRepository) {
        this.gameRepository = gameRepository;
        this.rosterService = rosterService;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public List<GamesDTO> getAllGames() {
        return gameRepository.findAll().stream()
                .map(game -> new GamesDTO(game.getId(), game.getDateTime(), game.getHomeTeamScore(),
                        game.getAwayTeamScore()))
                .collect(Collectors.toList());
    }

    @Override
    public Game getGameById(Integer id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        return game;
    }

    @Override
    @Transactional
    public Game createGameWithRoster(Game game, List<Roster> rosters) {

        game.setHomeTeamScore(0);
        game.setAwayTeamScore(0);
        Game savedGame = gameRepository.save(game);
        for (Roster roster : rosters) {
            roster.setGame(savedGame);
            rosterService.saveRoster(roster);
        }

        return savedGame;
    }

    @Override
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

        return gameRepository.save(game);
    }

    @Override
    @Transactional
    public void deleteGame(Integer id) {

        rosterService.deleteByGameId(id);

        gameRepository.deleteById(id);
    }

    @Override
    public void checkIfVotingIsComplete(Integer game_id, String team_color) {

        Integer totalVotesATeam = ratingRepository.countByRosterGameIdAndTeamColor(game_id, team_color);

        Game game = getGameById(game_id);
        Integer expectedVotesForATeam = (game.getRoster().size() / 2) * ((game.getRoster().size() / 2) - 1);

        if (totalVotesATeam == expectedVotesForATeam) {
            rosterService.updateRatingsForGame(game_id, team_color);
        }
    }

    @Override
    public void updateScoreWithGoal(Goal goal) {

        Game existingGame = getGameById(goal.getGame().getId());

        if (goal.getTeamColor().equalsIgnoreCase("white")) {
            existingGame.setHomeTeamScore(existingGame.getHomeTeamScore() + 1);
        } else {
            existingGame.setAwayTeamScore(existingGame.getAwayTeamScore() + 1);
        }

        gameRepository.save(existingGame);

    }
}
