package com.squad.squad.repository;

import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Persona;

@Repository
public interface PersonaRepository extends SecureJpaRepository<Persona, Integer> {
}