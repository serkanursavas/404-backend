package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.RatingDTO;
import com.squad.squad.service.RatingService;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Transactional
    @PostMapping("/saveRatings")
    public void saveRatings(@RequestBody List<RatingDTO> ratings) {
        ratingService.saveRating(ratings);
    }

}
