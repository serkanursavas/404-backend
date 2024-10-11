package com.squad.squad.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.squad.squad.dto.game.GameCreateRequestDTO;
import com.squad.squad.dto.game.GameResponseDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.dto.roster.RosterCreateDTO;
import com.squad.squad.dto.roster.RosterResponseDTO;
import com.squad.squad.dto.roster.RosterUpdateDTO;
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
    public Game findGameById(Integer id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));
    }

    @Override
    public GameResponseDTO getGameById(Integer id) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        List<RosterResponseDTO> rosters = rosterService.findRosterByGameId(id);

        for (RosterResponseDTO rosterDTO : rosters) {
            PlayerDTO playerDto = playerService.getPlayerById(rosterDTO.getPlayerId());
            rosterDTO.setPlayerName(playerDto.getName() + " " + playerDto.getSurname());
        }

        List<Goal> goals = game.getGoal();
        List<GoalDTO> goalDTOs = goals.stream()
                .map(goal -> {
                    GoalDTO goalDTO = new GoalDTO();
                    BeanUtils.copyProperties(goal, goalDTO);
                    PlayerDTO playerDto = playerService.getPlayerById(goal.getPlayer().getId());
                    goalDTO.setPlayerName(playerDto.getName());
                    return goalDTO;
                })
                .collect(Collectors.toList());

        for (GoalDTO goalDTO : goalDTOs) {
            PlayerDTO playerDto = playerService.getPlayerById(goalDTO.getPlayerId());
            goalDTO.setPlayerName(playerDto.getName());
        }

        GameResponseDTO gameDTO = new GameResponseDTO();
        BeanUtils.copyProperties(game, gameDTO);
        gameDTO.setRosters(rosters);
        gameDTO.setGoals(goalDTOs);

        return gameDTO;
    }

    @Override
    @Transactional
    public void createGame(GameCreateRequestDTO gameDto) {

        Game game = new Game();
        game.setDateTime(gameDto.getDateTime());
        game.setWeather(gameDto.getWeather());
        game.setLocation(gameDto.getLocation());

        List<Roster> rosters = new ArrayList<>();

        for (RosterCreateDTO rosterDTO : gameDto.getRosters()) {
            Roster roster = new Roster();
            roster.setGame(game);
            roster.setTeamColor(rosterDTO.getTeamColor().toUpperCase());

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
    }

    @Override
    @Transactional
    public void updateGame(Integer id, GameUpdateRequestDTO updatedGame) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        if (game.isPlayed()) {
            throw new IllegalArgumentException("Game has already been played. You cannot update game details.");
        }

        updateFieldIfNotNull(updatedGame.getLocation(), game::setLocation);
        updateFieldIfNotNull(updatedGame.getWeather(), game::setWeather);
        updateFieldIfNotNull(updatedGame.getDateTime(), game::setDateTime);

        gameRepository.save(game);

        if (updatedGame.getRosters() != null && !updatedGame.getRosters().isEmpty()) {

            List<Integer> rosterIds = updatedGame.getRosters().stream()
                    .map(RosterUpdateDTO::getId)
                    .collect(Collectors.toList());

            List<Roster> existingRosters = rosterService.findAllById(rosterIds);

            List<Integer> playerIds = updatedGame.getRosters().stream()
                    .map(RosterUpdateDTO::getPlayerId)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Integer, Player> playerMap = playerService.findAllById(playerIds).stream()
                    .collect(Collectors.toMap(Player::getId, player -> player));

            existingRosters.forEach(existingRoster -> {
                updatedGame.getRosters().stream()
                        .filter(rosterUpdateDTO -> rosterUpdateDTO.getId().equals(existingRoster.getId()))
                        .findFirst()
                        .ifPresent(rosterUpdateDTO -> {
                            updateFieldIfNotNull(rosterUpdateDTO.getTeamColor().toUpperCase(), existingRoster::setTeamColor);

                            Player player = playerMap.get(rosterUpdateDTO.getPlayerId());
                            if (player != null) {
                                existingRoster.setPlayer(player);
                            }
                        });
            });

            rosterService.updateAllRosters(existingRosters);
        }
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

        existingGame.setPlayed(true);
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