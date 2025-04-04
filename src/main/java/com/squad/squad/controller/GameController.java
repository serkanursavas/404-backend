package com.squad.squad.controller;

import com.squad.squad.dto.DTOvalidators.GameDTOValidator;
import com.squad.squad.dto.GameLocationDTO;
import com.squad.squad.dto.MvpDTO;
import com.squad.squad.dto.game.GameCreateRequestDTO;
import com.squad.squad.dto.game.GameResponseDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.dto.game.NextGameResponseDTO;
import com.squad.squad.mapper.GameLocationMapper;
import com.squad.squad.repository.GameLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.service.GameService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GameLocationRepository gameLocationRepository;
    private final GameDTOValidator gameDTOValidator;
    private final GameLocationMapper gameLocationMapper;

    @Autowired
    public GameController(GameService gameService, GameDTOValidator gameDTOValidator, GameLocationRepository gameLocationRepository, GameLocationMapper gameLocationMapper) {
        this.gameService = gameService;
        this.gameDTOValidator = gameDTOValidator;
        this.gameLocationRepository = gameLocationRepository;
        this.gameLocationMapper = gameLocationMapper;
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
        if (latestGame != null) {
            return ResponseEntity.ok(latestGame);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No upcoming matches available");
        }
    }

    @GetMapping("/getMvp")
    public ResponseEntity<MvpDTO> getMvpPlayer() {
        Optional<MvpDTO> mvpPlayer = gameService.getMvpPlayer();
        return mvpPlayer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/updateWeather/{id}")
    public ResponseEntity<?> updateWeather(@PathVariable Integer id, @RequestBody String weather) {

        gameService.updateWeather(id, weather);
        return ResponseEntity.ok("Game updated successfully");
    }


    @GetMapping("/getGameLocations")
    public ResponseEntity<List<GameLocationDTO>> getGameLocations() {
        Sort sort = Sort.by(Sort.Direction.ASC, "location");
        return ResponseEntity.ok(gameLocationMapper.gameLocationListToGameLocationDTOList(gameLocationRepository.findAll(sort)));
    }


}