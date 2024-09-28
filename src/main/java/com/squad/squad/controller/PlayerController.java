package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.entity.Player;
import com.squad.squad.service.PlayerService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("")
    public List<Player> getAllPlayers() {
        return playerService.getAllActivePlayers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Integer id) {

        Player player = playerService.getPlayerById(id);
        return ResponseEntity.ok(player);
    }

    @PutMapping("")
    public ResponseEntity<String> updatePlayer(@RequestBody Player updatedPlayer) {
        try {
            playerService.updatePlayer(updatedPlayer);
            return ResponseEntity.ok("Player updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePlayerById(@PathVariable Integer id) {
        try {
            playerService.deletePlayerById(id);
            return ResponseEntity.ok("Player updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
