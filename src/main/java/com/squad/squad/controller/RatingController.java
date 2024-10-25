package com.squad.squad.controller;

import com.squad.squad.dto.DTOvalidators.RatingDTOValidator;
import com.squad.squad.dto.rating.AddRatingRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.squad.squad.dto.RatingDTO;
import com.squad.squad.service.RatingService;

import jakarta.transaction.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final RatingDTOValidator ratingDTOValidator;

    public RatingController(RatingService ratingService, RatingDTOValidator ratingDTOValidator) {
        this.ratingService = ratingService;
        this.ratingDTOValidator = ratingDTOValidator;
    }

    @Transactional
    @PostMapping("/saveRatings")
    public ResponseEntity<?> saveRatings(@RequestBody List<AddRatingRequestDTO> ratings) {
        List<String> errors = ratingDTOValidator.validate(ratings);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        ratingService.saveRating(ratings);
        return ResponseEntity.ok("Your vote sent successfully");
    }

    @GetMapping("/checkVote/{playerId}")
    public boolean checkVote(@PathVariable Integer playerId) {
        return ratingService.checkVote(playerId);
    }
}