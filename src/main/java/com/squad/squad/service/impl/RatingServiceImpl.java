package com.squad.squad.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.RatingDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Rating;
import com.squad.squad.entity.Roster;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.service.GameService;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RatingService;
import com.squad.squad.service.RosterService;

import jakarta.transaction.Transactional;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final PlayerService playerService;
    private final RosterService rosterService;
    private final GameService gameService;

    private final PlayerMapper playerMapper = PlayerMapper.INSTANCE;

    public RatingServiceImpl(RatingRepository ratingRepository, PlayerService playerService,
            RosterService rosterService, GameService gameService) {
        this.ratingRepository = ratingRepository;
        this.playerService = playerService;
        this.rosterService = rosterService;
        this.gameService = gameService;
    }

    @Override
    @Transactional
    public void saveRating(List<RatingDTO> ratings) {

        Integer gameId = null;
        String teamColor = null;

        for (RatingDTO ratingDto : ratings) {
            Player existingPlayer = playerMapper.playerDTOtoPlayer(
                    playerService.getPlayerById(ratingDto.getPlayer_id()));
            Roster existingRoster = rosterService.getRosterById(ratingDto.getRoster_id());

            if (gameId == null && teamColor == null) {
                gameId = existingRoster.getGame().getId();
                teamColor = existingRoster.getTeamColor();
            }

            Rating rating = new Rating();
            rating.setPlayer(existingPlayer);
            rating.setRoster(existingRoster);
            rating.setRate(ratingDto.getRate());

            ratingRepository.save(rating);
        }

        gameService.checkIfVotingIsComplete(gameId, teamColor);
    }

    @Override
    public double calculateAvarageRating(Roster roster) {
        Double average = ratingRepository.findAverageRatingByRoster(roster);
        return average != null ? average : 0.0;
    }

    @Override
    @Transactional
    public void clearAllRatings() {
        ratingRepository.deleteAll();
    }

    @Override
    @Transactional
    public Integer countByRosterGameIdAndTeamColor(Integer gameId, String teamColor) {
        return ratingRepository.countByRosterGameIdAndTeamColor(gameId, teamColor);
    }
}
