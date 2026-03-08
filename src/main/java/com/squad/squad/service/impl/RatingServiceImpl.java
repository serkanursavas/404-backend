package com.squad.squad.service.impl;

import com.squad.squad.dto.rating.AddRatingRequestDTO;
import com.squad.squad.entity.*;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.repository.RosterPersonaRepository;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.service.BaseSquadService;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GroupAuthorizationService;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RatingService;
import com.squad.squad.service.RosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingServiceImpl extends BaseSquadService implements RatingService {

    private final RatingRepository ratingRepository;
    private final PlayerService playerService;
    private final GameService gameService;
    private RosterService rosterService;
    private final RosterRepository rosterRepository;
    private final RosterPersonaRepository rosterPersonaRepository;
    private final PlayerMapper playerMapper;
    private final GroupAuthorizationService groupAuthorizationService;

    @Autowired
    public RatingServiceImpl(RatingRepository ratingRepository, PlayerService playerService,
                             RosterService rosterService, GameService gameService,
                             RosterPersonaRepository rosterPersonaRepository, RosterRepository rosterRepository,
                             PlayerMapper playerMapper, GroupAuthorizationService groupAuthorizationService) {
        this.ratingRepository = ratingRepository;
        this.playerService = playerService;
        this.gameService = gameService;
        this.rosterPersonaRepository = rosterPersonaRepository;
        this.rosterRepository = rosterRepository;
        this.playerMapper = playerMapper;
        this.groupAuthorizationService = groupAuthorizationService;
    }

    @Autowired
    public void setRosterService(@Lazy RosterService rosterService) {
        this.rosterService = rosterService;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveRating(List<AddRatingRequestDTO> ratings) {
        Integer gameId = null;
        String voterTeamColor = null;

        if (ratings == null || ratings.isEmpty()) {
            throw new IllegalArgumentException("Ratings list cannot be null or empty");
        }

        // Get current player ID from group context
        Integer currentPlayerId = groupAuthorizationService.getCurrentPlayerId();

        for (AddRatingRequestDTO ratingDto : ratings) {
            Player existingPlayer = playerMapper.playerDTOToPlayer(
                    playerService.getPlayerById(ratingDto.getPlayerId()));

            Roster existingRoster = rosterService.getRosterById(ratingDto.getRosterId());

            if (gameId == null && voterTeamColor == null) {
                gameId = existingRoster.getGame().getId();

                Roster voterRoster = rosterService.getRosterByPlayerIdAndGameId(gameId, currentPlayerId);

                if (voterRoster == null) {
                    throw new IllegalStateException("Voter player is not in the roster for this game.");
                }

                if (!voterRoster.getHasVote()) {
                    voterRoster.setHasVote(true);
                    rosterRepository.save(voterRoster);
                }

                voterTeamColor = voterRoster.getTeamColor();

                Game existingGame = gameService.findGameById(gameId);

                if (!existingGame.isPlayed() || existingGame.isVoted()) {
                    throw new IllegalStateException("Voting is not allowed for this game. Check game state.");
                }
                if (((existingGame.getRoster().size() / 2) - 1) != ratings.size()) {
                    throw new IllegalStateException("You must vote for all players except yourself.");
                }
            }

            if (ratingDto.getPlayerId().equals(existingRoster.getPlayer().getId())) {
                throw new IllegalArgumentException("Players cannot vote for themselves.");
            }

            if (!voterTeamColor.equalsIgnoreCase(existingRoster.getTeamColor())) {
                throw new IllegalArgumentException("Players can only vote for their teammates.");
            }

            boolean hasAlreadyVoted = ratingRepository.existsByPlayerIdAndRosterId(currentPlayerId, ratingDto.getRosterId());
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
    public double calculateAverageRating(Roster roster) {
        Double average = ratingRepository.findAverageRatingByRoster(roster);
        return average != null ? average : 0.0;
    }

    @Override
    @Transactional
    public void updateRatingsForGame(Integer gameId, String teamColor) {
        List<Roster> rosters = rosterService.findRosterByGameIdAndTeamColor(gameId, teamColor);

        for (Roster roster : rosters) {
            double newRating = calculateAverageRating(roster);
            roster.setRating(newRating);
        }

        rosterService.saveAllRosters(rosters);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkIfVotingIsComplete(Integer gameId, String teamColor) {
        Integer totalVotesByTeam = ratingRepository.countByRosterGameIdAndTeamColor(gameId, teamColor);
        Game game = gameService.findGameById(gameId);
        int expectedVotes = (game.getRoster().size() / 2) * ((game.getRoster().size() / 2) - 1);

        if (totalVotesByTeam.equals(expectedVotes)) {
            updateRatingsForGame(gameId, teamColor);
        }

        // Count total votes for this game (not all ratings in the system)
        Integer totalVotes = ratingRepository.countByRosterGameIdAndTeamColor(gameId, "BLACK")
                + ratingRepository.countByRosterGameIdAndTeamColor(gameId, "WHITE");

        if (totalVotes.equals(expectedVotes * 2)) {
            List<Roster> rosters = rosterRepository.findAllByGameId(gameId);

            for (Roster roster : rosters) {
                List<RosterPersona> topRosterPersonas = rosterPersonaRepository.findTop3ByRosterId(roster.getId());

                roster.setPersona1(!topRosterPersonas.isEmpty() ? topRosterPersonas.get(0).getPersona().getId() : null);
                roster.setPersona2(topRosterPersonas.size() > 1 ? topRosterPersonas.get(1).getPersona().getId() : null);
                roster.setPersona3(topRosterPersonas.size() > 2 ? topRosterPersonas.get(2).getPersona().getId() : null);
            }

            rosterService.updateAllRosters(rosters);
            rosterService.updatePlayerGeneralRating(gameId);
            game.setVoted(true);

            Integer squadId = getSquadId();
            Integer mvpId = rosterPersonaRepository.findMvp(squadId);
            game.setMvpId(mvpId);

            gameService.updateVote(game);
        }
    }

    @Override
    @Transactional
    public void clearAllRatings() {
        Integer squadId = getSquadId();
        ratingRepository.deleteAllBySquadId(squadId);
    }

    public Integer getCurrentPlayerId() {
        return groupAuthorizationService.getCurrentPlayerId();
    }

    @Override
    public boolean checkVote(Integer playerId) {
        Integer squadId = getSquadId();
        return ratingRepository.existsByPlayerIdAndActiveGame(playerId, squadId);
    }
}
