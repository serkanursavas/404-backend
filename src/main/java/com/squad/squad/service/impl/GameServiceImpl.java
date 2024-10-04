package com.squad.squad.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.GoalDTO;
import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.enums.TeamColor;
import com.squad.squad.exception.GameNotFoundException;
import com.squad.squad.exception.NotFoundException;

import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.GameRepository;
import com.squad.squad.service.GameService;

import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RosterService;

import jakarta.transaction.Transactional;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RosterService rosterService;
    private final PlayerService playerService;
    private final PlayerMapper playerMapper = PlayerMapper.INSTANCE;

    public GameServiceImpl(GameRepository gameRepository, RosterService rosterService,
            PlayerService playerService) {
        this.gameRepository = gameRepository;
        this.rosterService = rosterService;
        this.playerService = playerService;
    }

    @Override
    public List<LatestGamesDTO> getAllGames() {
        return gameRepository.findAll().stream()
                .map(game -> new LatestGamesDTO(game.getId(), game.getDateTime(), game.getHomeTeamScore(),
                        game.getAwayTeamScore(), game.isPlayed()))
                .collect(Collectors.toList());
    }

    @Override
    public GameDTO getGameById(Integer id) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        List<RosterDTO> rosters = rosterService.findRosterByGameId(id);

        for (RosterDTO rosterDTO : rosters) {
            PlayerDTO playerDto = playerService.getPlayerById(rosterDTO.getPlayerId());
            rosterDTO.setPlayerName(playerDto.getName() + " " + playerDto.getSurname());
        }

        List<Goal> goals = game.getGoal();
        List<GoalDTO> goalDTOs = goals.stream()
                .map(goal -> {
                    GoalDTO goalDTO = new GoalDTO();
                    BeanUtils.copyProperties(goal, goalDTO);
                    PlayerDTO playerDto = playerService.getPlayerById(goal.getPlayer().getId());
                    goalDTO.setPlayer_name(playerDto.getName());
                    return goalDTO;
                })
                .collect(Collectors.toList());

        for (GoalDTO goalDTO : goalDTOs) {
            PlayerDTO playerDto = playerService.getPlayerById(goalDTO.getPlayer_id());
            goalDTO.setPlayer_name(playerDto.getName());
        }

        GameDTO gameDTO = new GameDTO();
        BeanUtils.copyProperties(game, gameDTO);
        gameDTO.setRosters(rosters);
        gameDTO.setGoals(goalDTOs);

        return gameDTO;
    }

    @Override
    @Transactional
    public GameDTO createGame(GameDTO gameDto) {
        // Set<Integer> playerIds = new HashSet<>();

        // for (RosterDTO roster : gameDTO.getRosters()) {
        // if (!playerIds.add(roster.getPlayerId())) {
        // return ResponseEntity.badRequest().body("Duplicate playerId found: " +
        // roster.getPlayerId());
        // }
        // }

        Game game = new Game();
        game.setDateTime(gameDto.getDateTime());
        game.setWeather(gameDto.getWeather());
        game.setLocation(gameDto.getLocation());
        game.setHomeTeamScore(gameDto.getHomeTeamScore());
        game.setAwayTeamScore(gameDto.getAwayTeamScore());

        List<Roster> rosters = new ArrayList<>();

        for (RosterDTO rosterDTO : gameDto.getRosters()) {
            Roster roster = new Roster();
            roster.setGame(game);
            roster.setTeamColor(rosterDTO.getTeamColor());

            Player player = playerMapper.playerDTOToPlayer(playerService.getPlayerById(rosterDTO.getPlayerId()));

            if (player == null) {
                throw new RuntimeException("Player not found with id: " + rosterDTO.getPlayerId());
            }

            roster.setPlayer(player);

            rosters.add(roster);
        }

        game.setHomeTeamScore(0);
        game.setAwayTeamScore(0);
        Game savedGame = gameRepository.save(game);

        rosters.forEach(roster -> roster.setGame(savedGame));
        rosterService.saveAllRosters(rosters);

        return gameDto;
    }

    @Override
    @Transactional
    public GameDTO updateGame(Integer id, GameDTO updatedGame) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        updateFieldIfNotNull(updatedGame.getLocation(), game::setLocation);
        updateFieldIfNotNull(updatedGame.getWeather(), game::setWeather);
        updateFieldIfNotNull(updatedGame.getDateTime(), game::setDateTime);

        game.setPlayed(updatedGame.isPlayed());

        gameRepository.save(game);
        return null;
    }

    @Override
    @Transactional
    public void updateScoreWithGoal(Goal goal) {

        Game existingGame = gameRepository.findById(goal.getGame().getId())
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + goal.getGame().getId()));

        TeamColor teamColor = TeamColor.fromString(goal.getTeamColor());

        if (teamColor == TeamColor.WHITE) {
            existingGame.setHomeTeamScore(existingGame.getHomeTeamScore() + 1);
        } else {
            existingGame.setAwayTeamScore(existingGame.getAwayTeamScore() + 1);
        }

        gameRepository.save(existingGame);

    }

    @Override
    @Transactional
    public void deleteGame(Integer id) {
        try {
            rosterService.deleteRosterByGameId(id);
            gameRepository.deleteById(id);
        } catch (Exception e) {
            throw new NotFoundException("Game or roster not found with game id: " + id);
        }
    }

    @Override
    public Game findById(Integer id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));
    }

    private <T> void updateFieldIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
