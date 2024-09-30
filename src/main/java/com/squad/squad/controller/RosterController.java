package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Roster;
import com.squad.squad.service.RosterService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/rosters")
public class RosterController {

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;

    }

    @GetMapping("")
    public List<Roster> getAllRosters() {
        return rosterService.getAllRosters();
    }

    @PutMapping("")
    public ResponseEntity<String> updateRosters(@RequestBody List<RosterDTO> rosters) {

        for (RosterDTO roster : rosters) {
            rosterService.updateRoster(roster);
        }

        return ResponseEntity.ok("success");

    }
}
