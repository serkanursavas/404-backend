package com.squad.squad.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.GameDTO;
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
    private RosterService rosterService;
    private final GameService gameService;

    private final PlayerMapper playerMapper = PlayerMapper.INSTANCE;

    public RatingServiceImpl(RatingRepository ratingRepository, PlayerService playerService,
            RosterService rosterService, GameService gameService) {
        this.ratingRepository = ratingRepository;
        this.playerService = playerService;

        this.gameService = gameService;
    }

    @Autowired
    public void setRosterService(@Lazy RosterService rosterService) {
        this.rosterService = rosterService;
    }

    @Override
    @Transactional
    public void saveRating(List<RatingDTO> ratings) {

        Integer gameId = null;
        String teamColor = null;

        for (RatingDTO ratingDto : ratings) {
            Player existingPlayer = playerMapper.playerDTOToPlayer(
                    playerService.getPlayerById(ratingDto.getPlayerId()));
            Roster existingRoster = rosterService.getRosterById(ratingDto.getPlayerId());

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

        checkIfVotingIsComplete(gameId, teamColor);
    }

    @Override
    public double calculateAvarageRating(Roster roster) {
        Double average = ratingRepository.findAverageRatingByRoster(roster);
        return average != null ? average : 0.0;
    }

    @Override
    @Transactional
    public void updateRatingsForGame(Integer gameId, String teamColor) {

        List<Roster> rosters = rosterService.findRosterByGameIdAndTeamColor(gameId, teamColor);

        for (Roster roster : rosters) {
            double newRating = calculateAvarageRating(roster);
            roster.setRating(newRating);
        }

        rosterService.saveAllRosters(rosters);
    }

    @Override
    @Transactional
    public void checkIfVotingIsComplete(Integer gameId, String teamColor) {

        Integer totalVotes = ratingRepository.countByRosterGameIdAndTeamColor(gameId, teamColor);
        GameDTO gameDto = gameService.getGameById(gameId);
        Integer expectedVotes = (gameDto.getRosters().size() / 2) * ((gameDto.getRosters().size() / 2) - 1);

        if (totalVotes.equals(expectedVotes)) {
            updateRatingsForGame(gameId, teamColor);
            rosterService.updatePlayerGeneralRating(gameId);
        }
    }

    @Override
    @Transactional
    public void clearAllRatings() {
        ratingRepository.deleteAll();
    }

}
