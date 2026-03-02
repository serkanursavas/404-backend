package com.squad.squad.repository;

import com.squad.squad.entity.GameLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameLocationRepository extends JpaRepository<GameLocation, Integer> {

    List<GameLocation> findBySquadIdOrderByLocationAsc(Integer squadId);
}
