package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Player;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GoalService;
import com.squad.squad.service.PlayerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final GameService gameService;
    private final PlayerService playerService;

    public GoalController(GoalService goalService, GameService gameService, PlayerService playerService) {
        this.goalService = goalService;
        this.gameService = gameService;
        this.playerService = playerService;
    }

    @GetMapping("")
    public List<Goal> getAllGoals() {
        return goalService.getAllGoals();
    }

    @PostMapping("")
    public ResponseEntity<String> addGoal(@RequestBody List<GoalDTO> goalDtos) {

        for (GoalDTO goalDto : goalDtos) {
            Game existingGame = gameService.getGameById(goalDto.getGame_id());
            Player existingPlayer = playerService.getPlayerById(goalDto.getPlayer_id());

            Goal goal = new Goal();
            goal.setGame(existingGame);
            goal.setPlayer(existingPlayer);
            goal.setTeamColor(goalDto.getTeam_color());

            goalService.addGoal(goal);
        }
        return ResponseEntity.ok("goals creation success");
    }

}
