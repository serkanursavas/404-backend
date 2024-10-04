package com.squad.squad.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Player;
import com.squad.squad.repository.GoalRepository;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GoalService;
import com.squad.squad.service.PlayerService;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final GameService gameService;
    private final PlayerService playerService;

    public GoalServiceImpl(GoalRepository goalRepository, GameService gameService, PlayerService playerService) {
        this.goalRepository = goalRepository;
        this.gameService = gameService;
        this.playerService = playerService;
    }

    @Override
    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    @Override
    public List<GoalDTO> getGoalsByGameId(Integer gameId) {
        return goalRepository.findGoalsByGameId(gameId).stream().map(
                goal -> new GoalDTO(gameId, goal.getPlayer().getId(), goal.getPlayer().getName(), goal.getTeamColor()))
                .collect(Collectors.toList());
    }

    @Override
    public void addGoals(List<GoalDTO> goalDtos) {

        Game existingGame = null;

        for (GoalDTO goalDto : goalDtos) {

            if (existingGame == null) {
                existingGame = gameService.getGameById(goalDto.getGame_id());
                existingGame.setPlayed(true);
                gameService.updateGame(existingGame.getId(), existingGame);
            }

            Player existingPlayer = playerService.getPlayerById(goalDto.getPlayer_id());

            Goal goal = new Goal();
            goal.setGame(existingGame);
            goal.setPlayer(existingPlayer);
            goal.setTeamColor(goalDto.getTeam_color());

            goalRepository.save(goal);

            gameService.updateScoreWithGoal(goal);
        }

    }

}
