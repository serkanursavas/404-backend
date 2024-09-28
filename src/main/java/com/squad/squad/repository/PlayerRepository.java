package com.squad.squad.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.squad.squad.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Integer> {

    List<Player> findByActive(boolean active);

}
