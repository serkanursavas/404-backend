package com.squad.squad.service.impl;

import java.util.List;
import java.util.function.Consumer;

import com.squad.squad.dto.roster.RosterResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.exception.RosterNotFoundException;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.mapper.RosterMapper;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RosterService;
import com.squad.squad.security.JwtGroupContextService;

import jakarta.transaction.Transactional;

@Service
public class RosterServiceImpl implements RosterService {

    private final RosterRepository rosterRepository;

    // private final PlayerService playerService; // Circular dependency removed
    private final RosterMapper rosterMapper;
    private final PlayerMapper playerMapper;
    private final JwtGroupContextService jwtGroupContextService;

    @Autowired
    public RosterServiceImpl(RosterRepository rosterRepository,
            RosterMapper rosterMapper, PlayerMapper playerMapper,
            JwtGroupContextService jwtGroupContextService) {
        this.rosterRepository = rosterRepository;
        // this.playerService = playerService; // Circular dependency removed
        this.rosterMapper = rosterMapper;
        this.playerMapper = playerMapper;
        this.jwtGroupContextService = jwtGroupContextService;
    }

    @Override
    public Roster getRosterById(Integer id) {
        return rosterRepository.findById(id)
                .orElseThrow(() -> new RosterNotFoundException("Roster not found with id: " + id));
    }

    @Override
    public List<RosterResponseDTO> findRosterByGameId(Integer gameId) {
        List<Roster> rosters = rosterRepository.findRosterByGameId(gameId,
                jwtGroupContextService.getCurrentApprovedGroupId());

        return rosterMapper.rostersToRosterResponseDTOs(rosters);
    }

    @Override
    @Transactional
    public void saveAllRosters(List<Roster> rosters) {
        rosterRepository.saveAll(rosters);
    }

    @Override
    public void updateAllRosters(List<Roster> rosters) {
        rosterRepository.saveAll(rosters);
    }

    @Override
    @Transactional
    public void deleteRosterByGameId(Integer id) {
        rosterRepository.deleteByGameId(id, jwtGroupContextService.getCurrentApprovedGroupId());
    }

    @Override
    @Transactional
    public void updatePlayerGeneralRating(Integer gameId) {

        List<Roster> gameRosters = rosterRepository.findRosterByGameId(gameId,
                jwtGroupContextService.getCurrentApprovedGroupId());

        for (Roster roster : gameRosters) {
            Player player = roster.getPlayer();

            List<Roster> playerRosters = rosterRepository.findRosterByPlayerId(player.getId(),
                    jwtGroupContextService.getCurrentApprovedGroupId());

            double generalRating = playerRosters.stream()
                    .mapToDouble(Roster::getRating)
                    .average()
                    .orElse(0.0);

            player.setRating(generalRating);
            // playerService.updatePlayer(playerMapper.playerToPlayerUpdateRequestDTO(player));
            // // Circular dependency removed
        }
    }

    @Override
    @Transactional
    public List<Roster> findRosterByGameIdAndTeamColor(Integer gameId, String teamColor) {
        return rosterRepository.findRosterByGameIdAndTeamColor(gameId, teamColor,
                jwtGroupContextService.getCurrentApprovedGroupId());
    }

    @Override
    public List<Roster> findAllById(List<Integer> rosterIds) {
        return rosterRepository.findAllById(rosterIds);
    }

    @Override
    public Roster getRosterByPlayerIdAndGameId(Integer gameId, Integer playerId) {
        List<Roster> rosters = rosterRepository.findByGameIdAndPlayerId(gameId, playerId,
                jwtGroupContextService.getCurrentApprovedGroupId());
        if (rosters.isEmpty()) {
            throw new RosterNotFoundException("Roster not found for gameId: " + gameId + " and playerId: " + playerId);
        }
        return rosters.get(0); // İlk elemanı döndür
    }

    private <T> void updateFieldIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}