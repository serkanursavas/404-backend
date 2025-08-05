package com.squad.squad.repository;

import com.squad.squad.entity.GameLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameLocationRepository extends SecureJpaRepository<GameLocation, Integer> {
}