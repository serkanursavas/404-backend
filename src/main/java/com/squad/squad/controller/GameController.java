package com.squad.squad.controller;

import com.squad.squad.dto.DTOvalidators.GameDTOValidator;
import org.springframework.http.ResponseEntity;
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
    private final GameDTOValidator gameDTOValidator;

    public GameController(GameService gameService, GameDTOValidator gameDTOValidator) {
        this.gameService = gameService;
        this.gameDTOValidator = gameDTOValidator;
    }

    @GetMapping("/getAllGames")
    public ResponseEntity<List<LatestGamesDTO>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/getGameById/{id}")
    public ResponseEntity<GameDTO> getGameById(@PathVariable Integer id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }

    @PostMapping("/createGame")
    public ResponseEntity<?> createGame(@RequestBody GameDTO gameDto) {
        List<String> errors = gameDTOValidator.validate(gameDto);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        gameService.createGame(gameDto);
        return ResponseEntity.ok("Game created successfully");
    }

    @PutMapping("/updateGame/{id}")
    public ResponseEntity<?> updateGame(@PathVariable Integer id, @RequestBody GameDTO updatedGame) {
        List<String> errors = gameDTOValidator.validate(updatedGame);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        gameService.updateGame(id, updatedGame);
        return ResponseEntity.ok("Game updated successfully");
    }

    @DeleteMapping("/deleteGame/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Integer id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}