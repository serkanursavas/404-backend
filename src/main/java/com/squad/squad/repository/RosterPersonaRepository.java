package com.squad.squad.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.RosterPersona;

@Repository
public interface RosterPersonaRepository extends JpaRepository<RosterPersona, Integer> {

    Optional<RosterPersona> findByRosterIdAndPersonaId(Integer rosterId, Integer personaId);

    @Query(value = "SELECT * FROM roster_persona WHERE persona_id != 68 and roster_id = :rosterId AND group_id = :groupId ORDER BY count DESC LIMIT 3", nativeQuery = true)
    List<RosterPersona> findTop3ByRosterId(@Param("rosterId") Integer rosterId, @Param("groupId") Integer groupId);

    @Query(value = "SELECT r.player_id\n" +
            "FROM roster_persona rp\n" +
            "left join roster r on rp.roster_id = r.id\n" +
            "WHERE persona_id = 68\n" +
            "  AND rp.group_id = :groupId\n" +
            "  AND r.group_id = :groupId\n" +
            "ORDER BY count DESC\n" +
            "limit 1", nativeQuery = true)
    Integer findMvp(@Param("groupId") Integer groupId);
}