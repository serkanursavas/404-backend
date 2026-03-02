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

    @Query(value = "SELECT * FROM roster_persona WHERE persona_id != 68 and roster_id = :rosterId ORDER BY count DESC LIMIT 3", nativeQuery = true)
    List<RosterPersona> findTop3ByRosterId(@Param("rosterId") Integer rosterId);

    @Query(value = "SELECT r.player_id\n" +
            "FROM roster_persona rp\n" +
            "left join roster r on rp.roster_id = r.id\n" +
            "left join game g on r.game_id = g.id\n" +
            "WHERE persona_id = 68 AND g.squad_id = :squadId\n" +
            "ORDER BY count DESC\n" +
            "limit 1", nativeQuery = true)
    Integer findMvp(@Param("squadId") Integer squadId);

    @Modifying
    @Query(value = "DELETE FROM roster_persona rp USING roster r, game g WHERE rp.roster_id = r.id AND r.game_id = g.id AND g.squad_id = :squadId", nativeQuery = true)
    void deleteAllBySquadId(@Param("squadId") Integer squadId);
}
