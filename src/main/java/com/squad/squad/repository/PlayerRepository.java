package com.squad.squad.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

    List<Player> findByActive(boolean active);

    Optional<Player> findByIdAndActive(Integer id, boolean active);

}
