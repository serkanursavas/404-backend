package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.service.RosterService;

import java.util.List;

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

    @GetMapping("/getAllRosters")
    public List<RosterDTO> getAllRosters() {
        return rosterService.getAllRosters();
    }

    @PutMapping("/updateAllRosters")
    public void updateAllRosters(@RequestBody List<RosterDTO> rosters) {
        rosterService.updateAllRosters(rosters);
    }
}
