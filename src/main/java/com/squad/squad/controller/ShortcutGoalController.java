package com.squad.squad.controller;

import com.squad.squad.dto.shortcut.ShortcutGoalRequestDTO;
import com.squad.squad.service.ShortcutGoalService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Apple Watch/Shortcuts entegrasyonu. Auth: ApiKeyAuthFilter (X-Api-Key header), normal JWT
 * akışına dahil değildir. Response'lar Shortcuts'ın "Speak Text" action'ıyla seslendirilecek
 * düz metindir.
 */
@RestController
@RequestMapping("/api/shortcut")
public class ShortcutGoalController {

    private final ShortcutGoalService shortcutGoalService;

    public ShortcutGoalController(ShortcutGoalService shortcutGoalService) {
        this.shortcutGoalService = shortcutGoalService;
    }

    @PostMapping(value = "/goals", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> addGoal(@RequestBody(required = false) ShortcutGoalRequestDTO request) {
        if (request == null || request.getPlayer() == null || request.getPlayer().isBlank()) {
            return ResponseEntity.badRequest().body("Oyuncu ismi gerekli.");
        }
        return ResponseEntity.ok(shortcutGoalService.addGoalByPlayerName(request.getPlayer()));
    }

    @PostMapping(value = "/goals/undo", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> undoGoal() {
        return ResponseEntity.ok(shortcutGoalService.undoLastGoal());
    }

    @GetMapping(value = "/score", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> score() {
        return ResponseEntity.ok(shortcutGoalService.getScoreText());
    }
}
