package com.squad.squad.repository;

import com.squad.squad.entity.RosterPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RosterPersonaRepository extends JpaRepository<RosterPersona, Integer> {

    Optional<RosterPersona> findByRosterIdAndPersonaId(Integer rosterId, Integer personaId);

    @Query(value = "SELECT * FROM roster_persona WHERE persona_id != 68 AND roster_id = :rosterId ORDER BY count DESC LIMIT 3", nativeQuery = true)
    List<RosterPersona> findTop3ByRosterId(@Param("rosterId") Integer rosterId);

    @Query(value = "SELECT r.player_id " +
            "FROM roster_persona rp " +
            "LEFT JOIN roster r ON rp.roster_id = r.id " +
            "LEFT JOIN game g ON r.game_id = g.id " +
            "WHERE persona_id = 68 AND g.squad_id = :squadId " +
            "ORDER BY rp.count DESC " +
            "LIMIT 1", nativeQuery = true)
    Integer findMvp(@Param("squadId") Integer squadId);

    @Modifying
    @Query(value = "DELETE FROM roster_persona rp USING roster r, game g WHERE rp.roster_id = r.id AND r.game_id = g.id AND g.squad_id = :squadId", nativeQuery = true)
    void deleteAllBySquadId(@Param("squadId") Integer squadId);

    @Query(value = "SELECT persona_id FROM roster_persona WHERE persona_id != 68 AND roster_id = :rosterId GROUP BY persona_id ORDER BY SUM(count) DESC LIMIT 3", nativeQuery = true)
    List<Integer> findTop3PersonaIdsByRosterId(@Param("rosterId") Integer rosterId);

    @Query(value = "SELECT r.player_id FROM roster_persona rp LEFT JOIN roster r ON rp.roster_id = r.id LEFT JOIN game g ON r.game_id = g.id WHERE persona_id = 68 AND g.squad_id = :squadId GROUP BY r.player_id ORDER BY SUM(rp.count) DESC LIMIT 1", nativeQuery = true)
    Integer findMvpGrouped(@Param("squadId") Integer squadId);
}
