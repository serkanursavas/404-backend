package com.squad.squad.repository;

import java.util.List;
import java.util.Optional;

import com.squad.squad.dto.TopListsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

    List<Player> findByActive(boolean active);

    Optional<Player> findByIdAndActive(Integer id, boolean active);

    @Query(value = "SELECT p.id as playerId, p.name, p.surname, p.rating " +
            "FROM player p " +
            "WHERE p.rating IS NOT NULL " +
            "ORDER BY p.rating DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findTopRatedPlayers();

    List<Player> findByIdIn(List<Integer> ids);
}