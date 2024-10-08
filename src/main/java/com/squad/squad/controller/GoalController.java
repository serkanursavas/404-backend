package com.squad.squad.controller;

import com.squad.squad.dto.DTOvalidators.GoalDTOValidator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.service.GoalService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final GoalDTOValidator goalDTOValidator;

    public GoalController(GoalService goalService, GoalDTOValidator goalDTOValidator) {
        this.goalService = goalService;
        this.goalDTOValidator = goalDTOValidator;
    }

    @GetMapping("/getAllGoals")
    public List<GoalDTO> getAllGoals() {
        return goalService.getAllGoals();
    }

    @PostMapping("/addGoals")
    public ResponseEntity<?> addGoals(@RequestBody List<GoalDTO> goalDtos) {
        List<String> errors = goalDTOValidator.validate(goalDtos);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        goalService.addGoals(goalDtos);
        return ResponseEntity.ok("Goals created successfully");
    }
}