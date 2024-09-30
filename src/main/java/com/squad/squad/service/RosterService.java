package com.squad.squad.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Roster;
import com.squad.squad.repository.RosterRepository;

@Service
public class RosterService {

    private final RosterRepository rosterRepository;
    private final RatingService ratingService;

    public RosterService(RosterRepository rosterRepository, RatingService ratingService) {
        this.rosterRepository = rosterRepository;
        this.ratingService = ratingService;
    }

    public List<Roster> getAllRosters() {
        return rosterRepository.findAll();
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

        existingRoster.setTeamColor(updatedRoster.getTeamColor());

        return rosterRepository.save(existingRoster);
    }

    public void updateRatingsForGame(Integer gameId) {

        List<Roster> rosters = rosterRepository.findRosterByGameId(gameId);

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
