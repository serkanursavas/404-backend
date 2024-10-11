package com.squad.squad.service.impl;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.squad.squad.dto.mapper.RosterDTOMapper;
import com.squad.squad.dto.roster.RosterResponseDTO;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.exception.RosterNotFoundException;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.mapper.RosterMapper;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RosterService;

import jakarta.transaction.Transactional;

@Service
public class RosterServiceImpl implements RosterService {

    private final RosterRepository rosterRepository;

    private final PlayerService playerService;
    private final RosterDTOMapper rosterDTOMapper;
    private final PlayerMapper playerMapper = PlayerMapper.INSTANCE;
    private final RosterMapper rosterMapper = RosterMapper.INSTANCE;

    public RosterServiceImpl(RosterRepository rosterRepository,
                             PlayerService playerService, RosterDTOMapper rosterDTOMapper) {
        this.rosterRepository = rosterRepository;
        this.playerService = playerService;
        this.rosterDTOMapper = rosterDTOMapper;
    }

    @Override
    public List<RosterDTO> getAllRosters() {
        List<Roster> rosters = rosterRepository.findAll();

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
    public List<RosterResponseDTO> findRosterByGameId(Integer gameId) {
        List<Roster> rosters = rosterRepository.findRosterByGameId(gameId);

        return rosterMapper.rostersToRosterResponseDTOs(rosters);
    }

    @Override
    @Transactional
    public Roster saveRoster(Roster roster) {
        return rosterRepository.save(roster);
    }

    @Override
    @Transactional
    public void saveAllRosters(List<Roster> rosters) {
        rosterRepository.saveAll(rosters);
    }

    @Override
    @Transactional
    public Roster updateRoster(RosterDTO updateRoster) {

        Roster existingRoster = rosterRepository.findById(updateRoster.getId())
                .orElseThrow(() -> new RosterNotFoundException("Roster not found with id: " + updateRoster.getId()));

        updateFieldIfNotNull(updateRoster.getTeamColor(), existingRoster::setTeamColor);
        updateFieldIfNotNull(updateRoster.getPlayerId(), playerId -> {
            Player existingPlayer = playerMapper.playerDTOToPlayer(playerService.getPlayerById(playerId));
            existingRoster.setPlayer(existingPlayer);
        });

        return rosterRepository.save(existingRoster);
    }

    @Override
    public void updateAllRosters(List<Roster> rosters) {
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

        List<Roster> gameRosters = rosterRepository.findRosterByGameId(gameId);

        for (Roster roster : gameRosters) {
            Player player = roster.getPlayer();

            List<Roster> playerRosters = rosterRepository.findRosterByPlayerId(player.getId());

            double generalRating = playerRosters.stream()
                    .mapToDouble(Roster::getRating)
                    .average()
                    .orElse(0.0);

            player.setRating(generalRating);
            playerService.updatePlayer(playerMapper.playerToPlayerDTO(player));
        }
    }

    @Override
    @Transactional
    public List<Roster> findRosterByGameIdAndTeamColor(Integer gameId, String teamColor) {
        return rosterRepository.findRosterByGameIdAndTeamColor(gameId, teamColor);
    }

    @Override
    public List<Roster> findAllById(List<Integer> rosterIds) {
        return rosterRepository.findAllById(rosterIds);
    }

    private <T> void updateFieldIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}