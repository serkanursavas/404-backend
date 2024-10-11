package com.squad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Game;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    Game findTopByOrderByDateTimeDesc();

    List<Game> findByIsPlayedFalse();
}