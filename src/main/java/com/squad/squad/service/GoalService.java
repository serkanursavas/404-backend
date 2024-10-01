package com.squad.squad.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Player;
import com.squad.squad.repository.GoalRepository;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final GameService gameService;
    private final PlayerService playerService;

    public GoalService(GoalRepository goalRepository, GameService gameService, PlayerService playerService) {
        this.goalRepository = goalRepository;
        this.gameService = gameService;
        this.playerService = playerService;
    }

    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    public List<GoalDTO> getGoalsByGameId(Integer game_id) {
        return goalRepository.findGoalsByGameId(game_id).stream().map(
                goal -> new GoalDTO(game_id, goal.getPlayer().getId(), goal.getPlayer().getName(), goal.getTeamColor()))
                .collect(Collectors.toList());
    }

    public void addGoals(List<GoalDTO> goalDtos) {

        for (GoalDTO goalDto : goalDtos) {
            Game existingGame = gameService.getGameById(goalDto.getGame_id());
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
