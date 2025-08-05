package com.squad.squad.repository;

import com.squad.squad.dto.MvpDTO;
import com.squad.squad.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends SecureJpaRepository<Game, Integer> {
    @Query("SELECT g FROM Game g WHERE g.groupId = :groupId ORDER BY g.dateTime DESC LIMIT 1")
    Game findTopByOrderByDateTimeDesc(@Param("groupId") Integer groupId);

    @Query("SELECT g FROM Game g WHERE g.isPlayed = false AND g.groupId = :groupId")
    Game findByIsPlayedFalseAndGroupId(@Param("groupId") Integer groupId);

    boolean existsByIsPlayedFalseOrIsVotedFalse();

    Page<Game> findAllByOrderByDateTimeDesc(Pageable pageable);

    @Query("SELECT new com.squad.squad.dto.MvpDTO(p.id, p.name, p.surname, p.photo, p.position, r.rating) " +
            "FROM Player p " +
            "JOIN Roster r ON p.id = r.player.id " +
            "JOIN Game g ON r.game.id = g.id " +
            "WHERE g.isPlayed = true AND g.isVoted = true AND g.dateTime = (" +
            "SELECT MAX(g2.dateTime) FROM Game g2 WHERE g2.isPlayed = true AND g2.isVoted = true" +
            ") " +
            "ORDER BY r.rating DESC")
    List<MvpDTO> findMvpPlayers();

    @Query(value = """
            SELECT
                p.id AS id,
                p.name AS name,
                p.surname AS surname,
                p.photo AS photo,
                p.position AS position,
                COALESCE(p.rating, 0.0) AS rating
            FROM game g
            INNER JOIN player p ON g.mvp_id = p.id
            WHERE g.is_played = true AND g.is_voted = true
            AND g.date_time = (SELECT MAX(g2.date_time) FROM Game g2 WHERE g2.is_played = true AND g2.is_voted = true AND g2.group_id = :groupId)
            AND g.group_id = :groupId
            ORDER BY g.date_time DESC
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findLatestVotedMvpRaw(@Param("groupId") Integer groupId);
}