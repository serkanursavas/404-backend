package com.squad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.squad.squad.entity.Game;

public interface GameRepository extends JpaRepository<Game, Integer> {

}
