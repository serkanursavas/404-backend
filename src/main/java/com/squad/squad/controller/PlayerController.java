package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.entity.Player;
import com.squad.squad.service.PlayerService;

import java.util.List;

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
    public ResponseEntity<Object> getPlayerById(@PathVariable Integer id) {
        Player player = playerService.getPlayerById(id);
        return ResponseEntity.ok(player);
    }

    @PutMapping("")
    public ResponseEntity<String> updatePlayer(@RequestBody Player updatedPlayer) {
        playerService.updatePlayer(updatedPlayer);
        return ResponseEntity.ok("Player updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayerById(@PathVariable Integer id) {
        playerService.deletePlayerById(id);
        return ResponseEntity.noContent().build();
    }

}
