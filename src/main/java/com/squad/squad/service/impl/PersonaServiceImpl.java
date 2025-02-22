package com.squad.squad.service.impl;

import com.squad.squad.dto.AddPersonaRequestDTO;
import com.squad.squad.entity.Persona;
import com.squad.squad.entity.PlayerPersona;
import com.squad.squad.entity.Roster;
import com.squad.squad.entity.RosterPersona;
import com.squad.squad.repository.PersonaRepository;
import com.squad.squad.repository.PlayerPersonaRepository;
import com.squad.squad.repository.RosterPersonaRepository;
import com.squad.squad.service.PersonaService;
import com.squad.squad.service.RosterService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final RosterService rosterService;
    private final PlayerPersonaRepository playerPersonaRepository;
    private final RosterPersonaRepository rosterPersonaRepository;

    public PersonaServiceImpl(PersonaRepository personaRepository,
                          RosterService rosterService,
                          PlayerPersonaRepository playerPersonaRepository,
                              RosterPersonaRepository rosterPersonaRepository) {
        this.personaRepository = personaRepository;
        this.rosterService = rosterService;
        this.playerPersonaRepository = playerPersonaRepository;
        this.rosterPersonaRepository = rosterPersonaRepository;
    }


    @Transactional
    public void savePersonas(List<AddPersonaRequestDTO> personas) {
        for (AddPersonaRequestDTO dto : personas) {
            Roster existingRoster = rosterService.getRosterById(dto.getRosterId());


            if (dto.getPersonaIds().size() > 3) {
                throw new IllegalArgumentException("Roster ID " + dto.getRosterId() + " has less than to 3 personas.");
            }

            for (Integer personaId : dto.getPersonaIds()) {

                Persona persona = personaRepository.findById(personaId)
                        .orElseThrow(() -> new IllegalArgumentException("Persona not found with ID: " + personaId));

                PlayerPersona playerPersona = playerPersonaRepository.findByPlayerIdAndPersonaId(
                                existingRoster.getPlayer().getId(), persona.getId())
                        .orElseGet(() -> {
                            PlayerPersona newPlayerPersona = new PlayerPersona();
                            newPlayerPersona.setPlayer(existingRoster.getPlayer());
                            newPlayerPersona.setPersona(persona);
                            newPlayerPersona.setCount(0);
                            return newPlayerPersona;
                        });

                // Persona sayısını artır
                playerPersona.setCount(playerPersona.getCount() + 1);
                playerPersonaRepository.save(playerPersona);

                // After
                RosterPersona rosterPersona = rosterPersonaRepository.findByRosterIdAndPersonaId(
                                existingRoster.getId(), persona.getId())
                        .orElseGet(() -> {
                            RosterPersona newRosterPersona = new RosterPersona();
                            newRosterPersona.setRoster(existingRoster);
                            newRosterPersona.setPersona(persona);
                            newRosterPersona.setCount(0);
                            return newRosterPersona;
                        });


                rosterPersona.setCount(rosterPersona.getCount() + 1);
                rosterPersonaRepository.save(rosterPersona);




            }
        }
    }
}