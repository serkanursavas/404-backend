package com.squad.squad.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.squad.squad.dto.goal.GoalAddRequestDTO;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Player;
import com.squad.squad.mapper.GameMapper;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.GoalRepository;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GoalService;
import com.squad.squad.service.PlayerService;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final GameService gameService;
    private final PlayerService playerService;
    private final GameMapper gameMapper = GameMapper.INSTANCE;
    private final PlayerMapper playerMapper = PlayerMapper.INSTANCE;

    public GoalServiceImpl(GoalRepository goalRepository, GameService gameService, PlayerService playerService) {
        this.goalRepository = goalRepository;
        this.gameService = gameService;
        this.playerService = playerService;
    }

    @Override
    public List<GoalDTO> getAllGoals() {
        return goalRepository.findAll().stream().map(
                        goal -> new GoalDTO(goal.getGame().getId(), goal.getPlayer().getId(), goal.getPlayer().getName(),
                                goal.getTeamColor()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GoalDTO> getGoalsByGameId(Integer gameId) {
        return goalRepository.findGoalsByGameId(gameId).stream().map(
                        goal -> new GoalDTO(gameId, goal.getPlayer().getId(), goal.getPlayer().getName(), goal.getTeamColor()))
                .collect(Collectors.toList());
    }

    @Override
    public void addGoals(List<GoalAddRequestDTO> goalDtos) {

        Game existingGame = null;

        for (GoalAddRequestDTO goalDto : goalDtos) {

            if (existingGame == null) {
                existingGame = gameService.findGameById(goalDto.getGameId());
                existingGame.setPlayed(true);

                gameService.updateGame(existingGame.getId(), gameMapper.gameToGameUpdateRequestDTO(existingGame));
            }

            Player existingPlayer = playerMapper.playerDTOToPlayer(playerService.getPlayerById(goalDto.getPlayerId()));

            Goal goal = new Goal();
            goal.setGame(existingGame);
            goal.setPlayer(existingPlayer);
            goal.setTeamColor(goalDto.getTeamColor());

            goalRepository.save(goal);

            gameService.updateScoreWithGoal(goal);
        }
    }
}