package com.squad.squad.controller;

import com.squad.squad.dto.GameLocationDTO;
import com.squad.squad.service.GameLocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game-locations")
public class GameLocationController {

    private final GameLocationService gameLocationService;

    public GameLocationController(GameLocationService gameLocationService) {
        this.gameLocationService = gameLocationService;
    }

    @GetMapping
    public ResponseEntity<List<GameLocationDTO>> getAll() {
        return ResponseEntity.ok(gameLocationService.getAll());
    }

    @PostMapping("/admin")
    public ResponseEntity<GameLocationDTO> create(@RequestBody GameLocationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameLocationService.create(dto));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<GameLocationDTO> update(@PathVariable Integer id, @RequestBody GameLocationDTO dto) {
        return ResponseEntity.ok(gameLocationService.update(id, dto));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        gameLocationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
