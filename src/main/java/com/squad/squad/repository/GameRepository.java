package com.squad.squad.repository;

import com.squad.squad.dto.MvpDTO;
import com.squad.squad.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {

    Optional<Game> findByIdAndSquadId(Integer id, Integer squadId);

    @Query("SELECT g FROM Game g LEFT JOIN FETCH g.roster WHERE g.id = :id")
    Optional<Game> findByIdWithRoster(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Game g WHERE g.id = :id")
    Optional<Game> findByIdWithLock(@Param("id") Integer id);

    Game findTopBySquadIdOrderByDateTimeDesc(Integer squadId);

    Game findBySquadIdAndIsPlayedFalse(Integer squadId);

    boolean existsBySquadIdAndIsPlayedFalseOrSquadIdAndIsVotedFalse(Integer squadId1, Integer squadId2);

    Page<Game> findAllBySquadIdOrderByDateTimeDesc(Integer squadId, Pageable pageable);

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
    WHERE g.is_played = true AND g.is_voted = true AND g.squad_id = :squadId
    AND g.date_time = (SELECT MAX(g2.date_time) FROM Game g2 WHERE g2.is_played = true AND g2.is_voted = true AND g2.squad_id = :squadId)
    ORDER BY g.date_time DESC
    LIMIT 1
    """, nativeQuery = true)
    List<Object[]> findLatestVotedMvpRaw(@Param("squadId") Integer squadId);

    // Legacy methods kept for backward compatibility
    Game findTopByOrderByDateTimeDesc();
    Game findByIsPlayedFalse();
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
}
