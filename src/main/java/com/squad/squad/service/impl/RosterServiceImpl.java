package com.squad.squad.service.impl;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.exception.RosterNotFoundException;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RatingService;
import com.squad.squad.service.RosterService;

import jakarta.transaction.Transactional;

public class RosterServiceImpl implements RosterService {

    private final RosterRepository rosterRepository;
    private final RatingService ratingService;
    private final PlayerService playerService;

    public RosterServiceImpl(RosterRepository rosterRepository, RatingService ratingService,
            PlayerService playerService) {
        this.rosterRepository = rosterRepository;
        this.ratingService = ratingService;
        this.playerService = playerService;
    }

    @Override
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

    @Override
    public Roster getRosterById(Integer id) {
        return rosterRepository.findById(id)
                .orElseThrow(() -> new RosterNotFoundException("Roster not found with id: " + id));
    }

    @Override
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

    @Override
    @Transactional
    public Roster saveRoster(Roster roster) {
        return rosterRepository.save(roster);
    }

    @Override
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

    @Override
    public void updateRosters(List<Roster> rosters) {
        rosterRepository.saveAll(rosters);
    }

    @Override
    @Transactional
    public void updateRatingsForGame(Integer gameId, String teamColor) {

        List<Roster> rosters = rosterRepository.findRosterByGameIdAndTeamColor(gameId, teamColor);

        for (Roster roster : rosters) {
            double newRating = ratingService.calculateAvarageRating(roster);
            roster.setRating(newRating);
        }

        rosterRepository.saveAll(rosters);
    }

    @Override
    @Transactional
    public void deleteRosterByGameId(Integer id) {
        rosterRepository.deleteByGameId(id);
    }

    @Override
    @Transactional
    public void updatePlayerGeneralRating(Integer gameId) {
        // O maçtaki tüm kadroları al
        List<Roster> gameRosters = rosterRepository.findRosterByGameId(gameId);

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