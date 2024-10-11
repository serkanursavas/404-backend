package com.squad.squad.controller;

import com.squad.squad.dto.DTOvalidators.GoalDTOValidator;
import com.squad.squad.dto.goal.AddGoalsRequestDTO;
import com.squad.squad.dto.goal.GoalAddRequestDTO;
import com.squad.squad.entity.Roster;
import com.squad.squad.service.GameService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.service.GoalService;

import org.springframework.http.ResponseEntity;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final GameService gameService;
    private final GoalDTOValidator goalDTOValidator;

    public GoalController(GoalService goalService, GameService gameService, GoalDTOValidator goalDTOValidator) {
        this.goalService = goalService;
        this.gameService = gameService;
        this.goalDTOValidator = goalDTOValidator;
    }

    @PostMapping("/admin/addGoals")
    public ResponseEntity<?> addGoals(@RequestBody AddGoalsRequestDTO requestDto) {

        Integer gameId = requestDto.getGameId();
        List<GoalAddRequestDTO> goalDtos = requestDto.getGoals();

        if (goalDtos == null || goalDtos.isEmpty()) {
            return ResponseEntity.badRequest().body("Goal list cannot be empty");
        }

        List<Roster> gameRosters = gameService.getRostersByGameId(gameId);
        
        List<String> errors = goalDTOValidator.validate(goalDtos, gameRosters);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        goalService.addGoals(requestDto);
        return ResponseEntity.ok("Goals created successfully");
    }
}