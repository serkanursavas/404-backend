package com.squad.squad.repository;

import com.squad.squad.entity.RosterPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface RosterPersonaRepository extends JpaRepository<RosterPersona, Integer> {

    Optional<RosterPersona> findByRosterIdAndPersonaId(Integer rosterId, Integer personaId);

    @Query(value = "SELECT * FROM roster_persona WHERE roster_id = :rosterId ORDER BY count DESC LIMIT 3", nativeQuery = true)
    List<RosterPersona> findTop3ByRosterId(@Param("rosterId") Integer rosterId);



}