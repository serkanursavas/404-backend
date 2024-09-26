package com.squad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.squad.squad.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Integer> {

}
