package com.squad.squad.repository;

import com.squad.squad.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends SecureJpaRepository<Persona, Integer> {
}