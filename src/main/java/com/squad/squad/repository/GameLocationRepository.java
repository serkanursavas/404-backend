package com.squad.squad.repository;

import org.springframework.stereotype.Repository;

import com.squad.squad.entity.GameLocation;

@Repository
public interface GameLocationRepository extends SecureJpaRepository<GameLocation, Integer> {
}