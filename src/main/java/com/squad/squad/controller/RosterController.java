package com.squad.squad.controller;

import com.squad.squad.dto.DTOvalidators.RosterDTOValidator;
import org.springframework.http.ResponseEntity;
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
    private final RosterDTOValidator rosterDTOValidator;

    public RosterController(RosterService rosterService, RosterDTOValidator rosterDTOValidator) {
        this.rosterService = rosterService;
        this.rosterDTOValidator = rosterDTOValidator;
    }

    @GetMapping("/getAllRosters")
    public ResponseEntity<List<RosterDTO>> getAllRosters() {
        return ResponseEntity.ok(rosterService.getAllRosters());
    }

    @PutMapping("/updateAllRosters")
    public ResponseEntity<?> updateAllRosters(@RequestBody List<RosterDTO> rosters) {
        List<String> errors = rosterDTOValidator.validate(rosters);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        rosterService.updateAllRosters(rosters);
        return ResponseEntity.ok("Rosters updated successfully");
    }
}