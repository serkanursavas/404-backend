package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.service.GameService;

import java.util.List;
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
