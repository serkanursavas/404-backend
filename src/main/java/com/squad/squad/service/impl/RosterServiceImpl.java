package com.squad.squad.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.roster.RosterResponseDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.exception.RosterNotFoundException;
import com.squad.squad.mapper.RosterMapper;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.security.JwtGroupContextService;
import com.squad.squad.service.RosterService;

import jakarta.transaction.Transactional;

@Service
public class RosterServiceImpl implements RosterService {

    private final RosterRepository rosterRepository;

    private final RosterMapper rosterMapper;
    private final JwtGroupContextService jwtGroupContextService;

    public RosterServiceImpl(RosterRepository rosterRepository,
            RosterMapper rosterMapper,
            JwtGroupContextService jwtGroupContextService) {
        this.rosterRepository = rosterRepository;
        this.rosterMapper = rosterMapper;
        this.jwtGroupContextService = jwtGroupContextService;
    }

    @Override
    public Roster getRosterById(Integer id) {
        return rosterRepository.findByIdAndGroupId(id, jwtGroupContextService.getCurrentApprovedGroupId())
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
        return rosterRepository.findAllByIdAndGroupId(rosterIds, jwtGroupContextService.getCurrentApprovedGroupId());
    }

    @Override
    public Roster getRosterByPlayerIdAndGameId(Integer gameId, Integer playerId) {
        List<Roster> rosters = rosterRepository.findByGameIdAndPlayerId(gameId, playerId,
                jwtGroupContextService.getCurrentApprovedGroupId());
        if (rosters.isEmpty()) {
            throw new RosterNotFoundException("Roster bulunamadı. GameId: " + gameId + " ve PlayerId: " + playerId);
        }
        return rosters.get(0); // İlk elemanı döndür
    }

}