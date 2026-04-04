package com.squad.squad.controller;

import com.squad.squad.dto.AddPersonaRequestDTO;
import com.squad.squad.entity.Persona;
import com.squad.squad.repository.PersonaRepository;
import com.squad.squad.service.GroupAuthorizationService;
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
    private final GroupAuthorizationService authService;

    public PersonaController(PersonaRepository personaRepository, PersonaService personaService, GroupAuthorizationService authService) {
        this.personaRepository = personaRepository;
        this.personaService = personaService;
        this.authService = authService;
    }

    @GetMapping("/all")
    public List<Persona> getAllPersonas() {
        return personaRepository.findAll();
    }

    @PostMapping("/savePersonas/{gameId}")
    public ResponseEntity<?> savePersonas(
            @PathVariable Integer gameId,
            @RequestBody List<AddPersonaRequestDTO> personas) {
        try {
            personaService.savePersonas(gameId, personas);
            return ResponseEntity.ok("Personas saved successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/recalculate/{gameId}")
    public ResponseEntity<?> recalculatePersonas(@PathVariable Integer gameId) {
        personaService.recalculatePersonasForGame(gameId);
        return ResponseEntity.ok("Personas recalculated for game " + gameId);
    }

    @PostMapping("/game/{gameId}/resubmit")
    public ResponseEntity<?> resubmitPersonas(
            @PathVariable Integer gameId,
            @RequestBody List<AddPersonaRequestDTO> personas) {
        try {
            personaService.resubmitPersonasForGame(gameId, personas);
            return ResponseEntity.ok("Personas submitted.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}