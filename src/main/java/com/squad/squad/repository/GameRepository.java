package com.squad.squad.repository;

import com.squad.squad.dto.MvpDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Game;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    Game findTopByOrderByDateTimeDesc();

    Game findByIsPlayedFalse();

    boolean existsByIsPlayedFalse();

    Page<Game> findAll(Pageable pageable);

    @Query("SELECT new com.squad.squad.dto.MvpDTO(p.id, p.name, p.surname, p.photo, p.position, r.rating) " +
            "FROM Player p " +
            "JOIN Roster r ON p.id = r.player.id " +
            "JOIN Game g ON r.game.id = g.id " +
            "WHERE g.isPlayed = true AND g.isVoted = true AND g.dateTime = (" +
            "SELECT MAX(g2.dateTime) FROM Game g2 WHERE g2.isPlayed = true AND g2.isVoted = true" +
            ") " +
            "ORDER BY r.rating DESC")
    List<MvpDTO> findMvpPlayers();
}