package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.RatingDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Rating;
import com.squad.squad.entity.Roster;
import com.squad.squad.service.GameService;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RatingService;
import com.squad.squad.service.RosterService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final PlayerService playerService;
    private final RosterService rosterService;
    private final GameService gameService;

    public RatingController(RatingService ratingService, PlayerService playerService, RosterService rosterService,
            GameService gameService) {
        this.ratingService = ratingService;
        this.playerService = playerService;
        this.rosterService = rosterService;
        this.gameService = gameService;
    }

    @PostMapping("")
    public ResponseEntity<String> submitRating(@RequestBody List<RatingDTO> ratings) {

        Integer game_id = null;

        for (RatingDTO ratingDto : ratings) {
            Player existingPlayer = playerService.getPlayerById(ratingDto.getPlayer_id());
            Roster existingRoster = rosterService.getRosterById(ratingDto.getRoster_id());

            if (game_id == null) {
                game_id = existingRoster.getGame().getId();
            }

            Rating rating = new Rating();
            rating.setPlayer(existingPlayer);
            rating.setRoster(existingRoster);
            rating.setRate(ratingDto.getRate());

            ratingService.saveRating(rating);
        }

        gameService.checkIfVotingIsComplete(game_id);

        return ResponseEntity.ok("success");
    }

}
