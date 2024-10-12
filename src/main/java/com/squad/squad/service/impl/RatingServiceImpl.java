package com.squad.squad.service.impl;

import java.util.List;

import com.squad.squad.dto.rating.AddRatingRequestDTO;
import com.squad.squad.entity.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final GameService gameService;
    private final PlayerMapper playerMapper = PlayerMapper.INSTANCE;
    private RosterService rosterService;

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
    public void saveRating(List<AddRatingRequestDTO> ratings) {
        Integer gameId = null;
        String voterTeamColor = null;

        if (ratings == null || ratings.isEmpty()) {
            throw new IllegalArgumentException("Ratings list cannot be null or empty");
        }

        for (AddRatingRequestDTO ratingDto : ratings) {
            Player existingPlayer = playerMapper.playerDTOToPlayer(
                    playerService.getPlayerById(ratingDto.getPlayerId()));

            Roster existingRoster = rosterService.getRosterById(ratingDto.getRosterId());

            if (gameId == null && voterTeamColor == null) {
                gameId = existingRoster.getGame().getId();

                Roster voterRoster = rosterService.getRosterByPlayerId(existingPlayer.getId());

                if (voterRoster == null) {
                    throw new IllegalStateException("Voter player is not in the roster for this game." + getCurrentPlayerId());
                }

                voterTeamColor = voterRoster.getTeamColor();

                Game existingGame = gameService.findGameById(gameId);
                if (!existingGame.isPlayed() || !existingGame.isVoteMode() || existingGame.isVoted()) {
                    throw new IllegalStateException("Voting is not allowed for this game. Check game state.");
                }
            }

            if (ratingDto.getPlayerId().equals(existingRoster.getPlayer().getId())) {
                throw new IllegalArgumentException("Players cannot vote for themselves.");
            }

            if (!voterTeamColor.equalsIgnoreCase(existingRoster.getTeamColor())) {
                throw new IllegalArgumentException("Players can only vote for their teammates.");
            }

            boolean hasAlreadyVoted = ratingRepository.existsByPlayerIdAndRosterId(existingPlayer.getId(), ratingDto.getRosterId());
            System.out.println(existingPlayer.getId() + " " + ratingDto.getRosterId());
            if (hasAlreadyVoted) {
                throw new IllegalArgumentException("You have already voted for this player.");
            }

            Rating rating = new Rating();
            rating.setPlayer(existingPlayer);
            rating.setRoster(existingRoster);
            rating.setRate(ratingDto.getRate());

            ratingRepository.save(rating);
        }

        checkIfVotingIsComplete(gameId, voterTeamColor);
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
        Game game = gameService.findGameById(gameId);
        Integer expectedVotes = (game.getRoster().size() / 2) * ((game.getRoster().size() / 2) - 1);

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

    public Integer getCurrentPlayerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return Integer.valueOf(userDetails.getUsername());
        }
        throw new IllegalStateException("Current user not found");
    }
}