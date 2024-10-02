package com.squad.squad.service;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.exception.RosterNotFoundException;
import com.squad.squad.repository.RosterRepository;

import jakarta.transaction.Transactional;

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
                .orElseThrow(() -> new RosterNotFoundException("Roster not found with id: " + id));
    }

    public List<RosterDTO> findRosterByGameId(Integer gameId) {
        List<Roster> rosters = rosterRepository.findRosterByGameId(gameId); // Roster'ları al

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

    @Transactional
    public Roster saveRoster(Roster roster) {
        return rosterRepository.save(roster);
    }

    @Transactional
    public Roster updateRoster(RosterDTO updatedRoster) {

        Roster existingRoster = rosterRepository.findById(updatedRoster.getId())
                .orElseThrow(() -> new RosterNotFoundException("Roster not found with id: " + updatedRoster.getId()));

        updateFieldIfNotNull(updatedRoster.getTeamColor(), existingRoster::setTeamColor);
        updateFieldIfNotNull(updatedRoster.getPlayerId(), playerId -> {
            Player existingPlayer = playerService.getPlayerById(playerId);
            existingRoster.setPlayer(existingPlayer);
        });

        return rosterRepository.save(existingRoster);
    }

    public void updateRosters(List<Roster> rosters) {
        rosterRepository.saveAll(rosters);
    }

    @Transactional
    public void updateRatingsForGame(Integer gameId, String teamColor) {
        List<Roster> rosters = rosterRepository.findRosterByGameIdAndTeamColor(gameId, teamColor);

        for (Roster roster : rosters) {
            double newRating = ratingService.calculateAvarageRating(roster);
            roster.setRating(newRating);
        }

        rosterRepository.saveAll(rosters);
    }

    @Transactional
    public void deleteByGameId(Integer id) {
        rosterRepository.deleteByGameId(id);
    }

    @Transactional
    public void updatePlayerGeneralRating(Integer game_id) {
        // O maçtaki tüm kadroları al
        List<Roster> gameRosters = rosterRepository.findRosterByGameId(game_id);

        for (Roster roster : gameRosters) {
            Player player = roster.getPlayer();

            // Oyuncunun tüm maçlarda oynadığı kadroları al
            List<Roster> playerRosters = rosterRepository.findRosterByPlayerId(player.getId());

            // Genel ortalama puanı hesapla
            double generalRating = playerRosters.stream()
                    .mapToDouble(Roster::getRating)
                    .average()
                    .orElse(0.0);

            // Player tablosundaki rating alanını güncelle
            player.setRating(generalRating);
            playerService.updatePlayer(player);
        }
    }

    private <T> void updateFieldIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
