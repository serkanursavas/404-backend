package com.squad.squad.controller;

import com.squad.squad.dto.AddPersonaRequestDTO;
import com.squad.squad.entity.Persona;
import com.squad.squad.repository.PersonaRepository;
import com.squad.squad.service.PersonaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {

    private final PersonaRepository personaRepository;
    private final PersonaService personaService;

    public PersonaController(PersonaRepository personaRepository, PersonaService personaService) {
        this.personaRepository = personaRepository;
        this.personaService = personaService;
    }

    @GetMapping("/all")
    public List<Persona> getAllPersonas() {
        return personaRepository.findAll();
    }

    @PostMapping("/savePersonas")
    public ResponseEntity<?> savePersonas(@RequestBody List<AddPersonaRequestDTO> personas) {
        try {
            personaService.savePersonas(personas);
            return ResponseEntity.ok("Personas saved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



}