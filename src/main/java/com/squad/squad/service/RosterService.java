package com.squad.squad.service;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.repository.RosterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RosterService {

    private final RosterRepository rosterRepository;
    private final RatingService ratingService;
    private final PlayerService playerService;

    public RosterService(RosterRepository rosterRepository, RatingService ratingService, PlayerService playerService) {
        this.rosterRepository = rosterRepository;
        this.ratingService = ratingService;
        this.playerService = playerService;
    }

    public List<RosterDTO> getAllRosters() {
        List<Roster> rosters = rosterRepository.findAll();

        // Roster -> RosterDTO dönüştürme
        return rosters.stream()
                .map(roster -> {
                    RosterDTO dto = new RosterDTO();
                    dto.setId(roster.getId());
                    dto.setTeamColor(roster.getTeamColor());
                    dto.setPlayerId(roster.getPlayer().getId());
                    dto.setRating(roster.getRating());
                    dto.setPlayerName(roster.getPlayer().getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Roster getRosterById(Integer id) {

        return rosterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roster not found with id: " + id));

    }

    public List<RosterDTO> findRosterByGameId(Integer gameId) {
        List<Roster> rosters = rosterRepository.findRosterByGameId(gameId); // Roster'ları al

        // Entity'yi DTO'ya dönüştür
        List<RosterDTO> rosterDTOs = rosters.stream()
                .map(roster -> {
                    RosterDTO dto = new RosterDTO();
                    dto.setId(roster.getId());
                    dto.setTeamColor(roster.getTeamColor());
                    dto.setPlayerId(roster.getPlayer().getId());
                    dto.setRating(roster.getRating());
                    dto.setPlayerName(roster.getPlayer().getName());
                    return dto;
                })
                .collect(Collectors.toList());

        return rosterDTOs;
    }

    public Roster saveRoster(Roster roster) {
        return rosterRepository.save(roster);
    }

    public Roster updateRoster(RosterDTO updatedRoster) {

        Roster existingRoster = rosterRepository.findById(updatedRoster.getId())
                .orElseThrow(() -> new RuntimeException("Roster not found with id: " + updatedRoster.getId()));

        if (updatedRoster.getTeamColor() != null) {
            existingRoster.setTeamColor(updatedRoster.getTeamColor());
        }
        if (updatedRoster.getPlayerId() != null) {
            Player existingPlayer = playerService.getPlayerById(updatedRoster.getPlayerId());

            existingRoster.setPlayer(existingPlayer);
        }

        return rosterRepository.save(existingRoster);
    }

    public void updateRatingsForGame(Integer gameId, String team_color) {

        List<Roster> rosters = rosterRepository.findRosterByGameIdAndTeamColor(gameId, team_color);

        for (Roster roster : rosters) {
            double newRating = ratingService.calculateAvarageRating(roster);
            roster.setRating(newRating);
            rosterRepository.save(roster);
        }
    }

    public void deleteByGameId(Integer id) {
        rosterRepository.deleteByGameId(id);
    }

}
