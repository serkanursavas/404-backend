package com.squad.squad.service.impl;

import com.squad.squad.dto.rating.AddRatingRequestDTO;
import com.squad.squad.entity.*;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.repository.RosterPersonaRepository;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.service.GameService;
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
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final PlayerService playerService;
    private final GameService gameService;
    private RosterService rosterService;
    private final RosterRepository rosterRepository;
    private final RosterPersonaRepository rosterPersonaRepository;
    private final PlayerMapper playerMapper;

    @Autowired public RatingServiceImpl(RatingRepository ratingRepository, PlayerService playerService,
                                        RosterService rosterService, GameService gameService, RosterPersonaRepository rosterPersonaRepository, RosterRepository rosterRepository, PlayerMapper playerMapper) {
        this.ratingRepository = ratingRepository;
        this.playerService = playerService;
        this.gameService = gameService;
        this.rosterPersonaRepository = rosterPersonaRepository;
        this.rosterRepository = rosterRepository;
        this.playerMapper = playerMapper;
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

        for (AddRatingRequestDTO ratingDto : ratings) {
            Player existingPlayer = playerMapper.playerDTOToPlayer(
                    playerService.getPlayerById(ratingDto.getPlayerId()));

            Roster existingRoster = rosterService.getRosterById(ratingDto.getRosterId());

            if (gameId == null && voterTeamColor == null) {
                gameId = existingRoster.getGame().getId();

                Roster voterRoster = rosterService.getRosterByPlayerIdAndGameId(gameId, existingPlayer.getId());

                if (voterRoster == null) {
                    throw new IllegalStateException("Voter player is not in the roster for this game." + getCurrentPlayerId());
                }

                voterTeamColor = voterRoster.getTeamColor();

                Game existingGame = gameService.findGameById(gameId);

                if (!existingGame.isPlayed() || existingGame.isVoted() ) {
                    throw new IllegalStateException("Voting is not allowed for this game. Check game state.");
                }
                if ( ((existingGame.getRoster().size() /2 ) - 1) != ratings.size()) {
                    throw new IllegalStateException("You must vote for all players except yourself.");
                }
            }

            if (ratingDto.getPlayerId().equals(existingRoster.getPlayer().getId())) {
                throw new IllegalArgumentException("Players cannot vote for themselves.");
            }

            if (!voterTeamColor.equalsIgnoreCase(existingRoster.getTeamColor())) {
                throw new IllegalArgumentException("Players can only vote for their teammates.");
            }

            boolean hasAlreadyVoted = ratingRepository.existsByPlayerIdAndRosterId(existingPlayer.getId(), ratingDto.getRosterId());
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

        Integer totalVotes = (int) ratingRepository.count();

        if (totalVotes.equals(expectedVotes * 2)) {

            List<Roster> rosters = rosterRepository.findAllByGameId(gameId);



            for (Roster roster : rosters) {
                List<RosterPersona> topRosterPersonas = rosterPersonaRepository.findTop3ByRosterId(roster.getId());

                // Eğer 3 kayıt yoksa null değer atamaktan kaçınmak için:
                roster.setPersona1(!topRosterPersonas.isEmpty() ? topRosterPersonas.get(0).getPersona().getId() : null);
                roster.setPersona2(topRosterPersonas.size() > 1 ? topRosterPersonas.get(1).getPersona().getId() : null);
                roster.setPersona3(topRosterPersonas.size() > 2 ? topRosterPersonas.get(2).getPersona().getId() : null);
                System.out.println("Roster: " + roster.getId());
                System.out.println("Persona1: " + roster.getPersona1());
                System.out.println("Persona2: " + roster.getPersona2());
                System.out.println("Persona3: " + roster.getPersona3());
            }

            rosterService.updateAllRosters(rosters);

            rosterService.updatePlayerGeneralRating(gameId);
            game.setVoted(true);
            gameService.updateVote(game);
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

    @Override
    public boolean checkVote(Integer playerId) {
        return ratingRepository.existsByPlayerId(playerId);
    }
}