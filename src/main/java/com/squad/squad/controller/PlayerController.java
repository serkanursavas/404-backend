package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.service.PlayerService;

import java.util.List;

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

    @GetMapping("/getAllPlayers")
    public List<PlayerDTO> getAllPlayers() {
        return playerService.getAllActivePlayers();
    }

    @GetMapping("/getPlayerById/{id}")
    public PlayerDTO getPlayerById(@PathVariable Integer id) {
        return playerService.getPlayerById(id);
    }

    @PutMapping("/updatePlayer")
    public PlayerDTO updatePlayer(@RequestBody PlayerDTO updatedPlayer) {
        return playerService.updatePlayer(updatedPlayer);

    }

    @DeleteMapping("/deletePlayerById/{id}")
    public void deletePlayerById(@PathVariable Integer id) {
        playerService.deletePlayerById(id);
    }

}
