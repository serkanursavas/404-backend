package com.squad.squad.controller;

import com.squad.squad.dto.DTOvalidators.PlayerDTOValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.service.PlayerService;

import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerService playerService;
    private final PlayerDTOValidator playerDTOValidator;

    public PlayerController(PlayerService playerService, PlayerDTOValidator playerDTOValidator) {
        this.playerService = playerService;
        this.playerDTOValidator = playerDTOValidator;
    }

    @GetMapping("/getAllPlayers")
    public ResponseEntity<List<PlayerDTO>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllActivePlayers());
    }

    @GetMapping("/getPlayerById/{id}")
    public ResponseEntity<PlayerDTO> getPlayerById(@PathVariable Integer id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @PutMapping("/updatePlayer")
    public ResponseEntity<?> updatePlayer(@RequestBody PlayerDTO updatedPlayer) {
        List<String> errors = playerDTOValidator.validate(updatedPlayer);

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        playerService.updatePlayer(updatedPlayer);
        return ResponseEntity.ok("Player updated successfully");
    }

    @DeleteMapping("/deletePlayerById/{id}")
    public ResponseEntity<Void> deletePlayerById(@PathVariable Integer id) {
        playerService.deletePlayerById(id);
        return ResponseEntity.noContent().build();
    }
}