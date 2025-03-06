package com.squad.squad.repository;

import com.squad.squad.entity.PlayerPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerPersonaRepository extends JpaRepository<PlayerPersona, Integer> {
    Optional<PlayerPersona> findByPlayerIdAndPersonaId(Integer playerId, Integer personaId);

    @Query(value = "SELECT pp.*\n" +
            "FROM player_persona pp\n" +
            "JOIN persona per ON pp.persona_id = per.id\n" +
            "WHERE pp.player_id = :playerId\n" +
            "ORDER BY pp.count DESC\n" +
            "LIMIT 3\n", nativeQuery = true)
    List<PlayerPersona> getPersonas(Integer playerId);
}