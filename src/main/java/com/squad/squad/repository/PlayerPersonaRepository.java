package com.squad.squad.repository;

import com.squad.squad.entity.PlayerPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerPersonaRepository extends SecureJpaRepository<PlayerPersona, Integer> {

    @Query("SELECT pp FROM PlayerPersona pp WHERE pp.player.id = :playerId AND pp.persona.id = :personaId AND pp.groupId = :groupId")
    Optional<PlayerPersona> findByPlayerIdAndPersonaId(@Param("playerId") Integer playerId,
            @Param("personaId") Integer personaId, @Param("groupId") Integer groupId);
}