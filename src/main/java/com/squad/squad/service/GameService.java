package com.squad.squad.service;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.GamesDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Roster;
import com.squad.squad.enums.TeamColor;
import com.squad.squad.exception.GameNotFoundException;
import com.squad.squad.repository.GameRepository;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.repository.RosterRepository;

import jakarta.transaction.Transactional;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final RosterService rosterService;
    private final RatingRepository ratingRepository;
    private final RosterRepository rosterRepository;

    public GameService(GameRepository gameRepository, RosterService rosterService, RatingRepository ratingRepository,
            RosterRepository rosterRepository) {
        this.gameRepository = gameRepository;
        this.rosterService = rosterService;
        this.ratingRepository = ratingRepository;
        this.rosterRepository = rosterRepository;
    }

    public List<GamesDTO> getAllGames() {
        return gameRepository.findAll().stream()
                .map(game -> new GamesDTO(game.getId(), game.getDateTime(), game.getHomeTeamScore(),
                        game.getAwayTeamScore(), game.isPlayed()))
                .collect(Collectors.toList());
    }

    public Game getGameById(Integer id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));
    }

    @Transactional
    public Game createGameWithRoster(Game game, List<Roster> rosters) {

        game.setHomeTeamScore(0);
        game.setAwayTeamScore(0);
        Game savedGame = gameRepository.save(game);

        rosters.forEach(roster -> roster.setGame(savedGame));
        rosterRepository.saveAll(rosters);

        return savedGame;
    }

    @Transactional
    public Game updateGame(Integer id, Game updatedGame) {

        Game game = getGameById(id);

        updateFieldIfNotNull(updatedGame.getLocation(), game::setLocation);
        updateFieldIfNotNull(updatedGame.getWeather(), game::setWeather);
        updateFieldIfNotNull(updatedGame.getDateTime(), game::setDateTime);

        game.setPlayed(updatedGame.isPlayed());

        return gameRepository.save(game);
    }

    @Transactional
    public void updateScoreWithGoal(Goal goal) {

        Game existingGame = getGameById(goal.getGame().getId());

        TeamColor teamColor = TeamColor.fromString(goal.getTeamColor());

        if (teamColor == TeamColor.WHITE) {
            existingGame.setHomeTeamScore(existingGame.getHomeTeamScore() + 1);
        } else {
            existingGame.setAwayTeamScore(existingGame.getAwayTeamScore() + 1);
        }

        gameRepository.save(existingGame);

    }

    @Transactional
    public void deleteGame(Integer id) {

        rosterService.deleteByGameId(id);
        gameRepository.deleteById(id);
    }

    @Transactional
    public void checkIfVotingIsComplete(Integer game_id, String team_color) {

        Integer totalVotes = ratingRepository.countByRosterGameIdAndTeamColor(game_id, team_color);
        Game game = getGameById(game_id);
        Integer expectedVotes = (game.getRoster().size() / 2) * ((game.getRoster().size() / 2) - 1);

        if (totalVotes.equals(expectedVotes)) {
            rosterService.updateRatingsForGame(game_id, team_color);
            rosterService.updatePlayerGeneralRating(game_id);
        }
    }

    private <T> void updateFieldIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
