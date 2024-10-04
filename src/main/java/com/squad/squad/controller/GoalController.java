package com.squad.squad.controller;

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

    public GoalController(GoalService goalService) {
        this.goalService = goalService;

    }

    @GetMapping("/getAllGoals")
    public List<GoalDTO> getAllGoals() {
        return goalService.getAllGoals();
    }

    @PostMapping("/addGoals")
    public ResponseEntity<String> addGoals(@RequestBody List<GoalDTO> goalDtos) {

        goalService.addGoals(goalDtos);
        return ResponseEntity.ok("goals creation success");
    }

}
