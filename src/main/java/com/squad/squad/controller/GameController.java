package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.dto.GoalDTO;
import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GoalService;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RosterService;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/games")

public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/getAllGames")
    public List<LatestGamesDTO> getAllGames() {
        return gameService.getAllGames();
    }

    @GetMapping("/getGameById/{id}")
    public GameDTO getGameById(@PathVariable Integer id) {
        return gameService.getGameById(id);
    }

    @PostMapping("/createGame")
    public GameDTO createGame(@RequestBody GameDTO gameDto) {
        return gameService.createGame(gameDto);
    }

    @PutMapping("/updateGame/{id}")
    public GameDTO updateGame(@PathVariable Integer id, @RequestBody GameDTO updatedGame) {
        return gameService.updateGame(id, updatedGame);
    }

    @DeleteMapping("/deleteGame/{id}")
    public void deleteGame(@PathVariable Integer id) {
        gameService.deleteGame(id);
    }

}
