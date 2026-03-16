package com.squad.squad.repository;

import com.squad.squad.entity.GameLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameLocationRepository extends JpaRepository<GameLocation, Integer> {

    List<GameLocation> findBySquadIdOrderByLocationAsc(Integer squadId);

    List<GameLocation> findBySquadIdAndActiveTrueOrderByLocationAsc(Integer squadId);

    Optional<GameLocation> findByIdAndSquadId(Integer id, Integer squadId);
}
