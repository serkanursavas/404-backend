package com.squad.squad.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    Game findTopByOrderByDateTimeDesc();

    Game findByIsPlayedFalse();

    boolean existsByIsPlayedFalse();

    Page<Game> findAll(Pageable pageable);
}