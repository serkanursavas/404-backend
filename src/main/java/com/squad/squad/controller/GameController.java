package com.squad.squad.controller;

import com.squad.squad.dto.DTOvalidators.GameDTOValidator;
import com.squad.squad.dto.game.GameCreateRequestDTO;
import com.squad.squad.dto.game.GameResponseDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.dto.game.NextGameResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.service.GameService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Page<LatestGamesDTO>> getAllGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LatestGamesDTO> gamesPage = gameService.getAllGames(pageable);
        return ResponseEntity.ok(gamesPage);
    }

    @GetMapping("/getGameById/{id}")
    public ResponseEntity<GameResponseDTO> getGameById(@PathVariable Integer id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }

    @PostMapping("/admin/createGame")
    public ResponseEntity<?> createGame(@RequestBody GameCreateRequestDTO gameDto) {
        List<String> errors = gameDTOValidator.validate(gameDto);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        gameService.createGame(gameDto);
        return ResponseEntity.ok("Game created successfully");
    }

    @PutMapping("/admin/updateGame/{id}")
    public ResponseEntity<?> updateGame(@PathVariable Integer id, @RequestBody GameUpdateRequestDTO updatedGame) {
        List<String> errors = gameDTOValidator.updateValidate(updatedGame);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        gameService.updateGame(id, updatedGame);
        return ResponseEntity.ok("Game updated successfully");
    }

    @DeleteMapping("/admin/deleteGame/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Integer id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getNextGame")
    public ResponseEntity<?> getNextGame() {
        GameResponseDTO latestGame = gameService.getLatestGame();
        System.out.println("getNExtGameeeeeeeeee");
        if (latestGame != null) {
            System.out.println("Successssssssssssss");
            return ResponseEntity.ok(latestGame);
        } else {
            System.out.println("failedddddddddddddddddddd");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No upcoming matches available");
        }
    }
}