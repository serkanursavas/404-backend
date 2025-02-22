package com.squad.squad.repository;

import com.squad.squad.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Persona findByName(String name);
}