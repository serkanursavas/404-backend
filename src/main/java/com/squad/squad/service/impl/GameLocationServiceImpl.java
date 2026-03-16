package com.squad.squad.service.impl;

import com.squad.squad.dto.GameLocationDTO;
import com.squad.squad.entity.GameLocation;
import com.squad.squad.entity.Squad;
import com.squad.squad.mapper.GameLocationMapper;
import com.squad.squad.repository.GameLocationRepository;
import com.squad.squad.repository.SquadRepository;
import com.squad.squad.service.BaseSquadService;
import com.squad.squad.service.GameLocationService;
import com.squad.squad.service.GroupAuthorizationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameLocationServiceImpl extends BaseSquadService implements GameLocationService {

    private final GameLocationRepository gameLocationRepository;
    private final GameLocationMapper gameLocationMapper;
    private final GroupAuthorizationService authService;
    private final SquadRepository squadRepository;

    public GameLocationServiceImpl(GameLocationRepository gameLocationRepository,
                                   GameLocationMapper gameLocationMapper,
                                   GroupAuthorizationService authService,
                                   SquadRepository squadRepository) {
        this.gameLocationRepository = gameLocationRepository;
        this.gameLocationMapper = gameLocationMapper;
        this.authService = authService;
        this.squadRepository = squadRepository;
    }

    @Override
    public List<GameLocationDTO> getAll() {
        return gameLocationMapper.gameLocationListToGameLocationDTOList(
                gameLocationRepository.findBySquadIdAndActiveTrueOrderByLocationAsc(getSquadId()));
    }

    @Override
    public GameLocationDTO create(GameLocationDTO dto) {
        authService.requireAdmin();
        Integer squadId = getSquadId();
        Squad squad = squadRepository.findById(squadId)
                .orElseThrow(() -> new IllegalStateException("Squad not found"));

        GameLocation location = new GameLocation();
        location.setLocation(dto.getLocation());
        location.setAddress(dto.getAddress());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setSquad(squad);

        return gameLocationMapper.gameLocationToGameLocationDTO(gameLocationRepository.save(location));
    }

    @Override
    public GameLocationDTO update(Integer id, GameLocationDTO dto) {
        authService.requireAdmin();
        GameLocation location = gameLocationRepository.findByIdAndSquadId(id, getSquadId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        location.setLocation(dto.getLocation());
        location.setAddress(dto.getAddress());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());

        return gameLocationMapper.gameLocationToGameLocationDTO(gameLocationRepository.save(location));
    }

    @Override
    public void delete(Integer id) {
        authService.requireAdmin();
        GameLocation location = gameLocationRepository.findByIdAndSquadId(id, getSquadId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        location.setActive(false);
        gameLocationRepository.save(location);
    }
}
