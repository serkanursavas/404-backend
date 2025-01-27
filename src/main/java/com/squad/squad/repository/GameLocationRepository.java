package com.squad.squad.repository;

import com.squad.squad.entity.GameLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameLocationRepository extends JpaRepository<GameLocation, Integer> {
}