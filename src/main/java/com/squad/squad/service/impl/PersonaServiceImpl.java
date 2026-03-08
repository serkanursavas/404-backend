package com.squad.squad.service.impl;

import com.squad.squad.dto.AddPersonaRequestDTO;
import com.squad.squad.entity.Persona;
import com.squad.squad.entity.PlayerPersona;
import com.squad.squad.entity.Roster;
import com.squad.squad.entity.RosterPersona;
import com.squad.squad.entity.Squad;
import com.squad.squad.repository.PersonaRepository;
import com.squad.squad.repository.PlayerPersonaRepository;
import com.squad.squad.repository.RosterPersonaRepository;
import com.squad.squad.repository.SquadRepository;
import com.squad.squad.service.BaseSquadService;
import com.squad.squad.service.PersonaService;
import com.squad.squad.service.RosterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonaServiceImpl extends BaseSquadService implements PersonaService {

    private final PersonaRepository personaRepository;
    private final RosterService rosterService;
    private final PlayerPersonaRepository playerPersonaRepository;
    private final RosterPersonaRepository rosterPersonaRepository;
    private final SquadRepository squadRepository;

    public PersonaServiceImpl(PersonaRepository personaRepository,
                              RosterService rosterService,
                              PlayerPersonaRepository playerPersonaRepository,
                              RosterPersonaRepository rosterPersonaRepository,
                              SquadRepository squadRepository) {
        this.personaRepository = personaRepository;
        this.rosterService = rosterService;
        this.playerPersonaRepository = playerPersonaRepository;
        this.rosterPersonaRepository = rosterPersonaRepository;
        this.squadRepository = squadRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void savePersonas(List<AddPersonaRequestDTO> personas) {
        Integer squadId = getSquadId();
        Squad squad = squadRepository.findById(squadId).orElse(null);

        for (AddPersonaRequestDTO dto : personas) {
            Roster existingRoster = rosterService.getRosterById(dto.getRosterId());

            if (dto.getPersonaIds().size() > 3) {
                throw new IllegalArgumentException("Roster ID " + dto.getRosterId() + " has less than to 3 personas.");
            }

            for (Integer personaId : dto.getPersonaIds()) {
                Persona persona = personaRepository.findById(personaId)
                        .orElseThrow(() -> new IllegalArgumentException("Persona not found with ID: " + personaId));

                PlayerPersona playerPersona = playerPersonaRepository.findByPlayerIdAndPersonaIdAndSquadId(
                                existingRoster.getPlayer().getId(), persona.getId(), squadId)
                        .orElseGet(() -> {
                            PlayerPersona newPlayerPersona = new PlayerPersona();
                            newPlayerPersona.setPlayer(existingRoster.getPlayer());
                            newPlayerPersona.setPersona(persona);
                            newPlayerPersona.setSquad(squad);
                            newPlayerPersona.setCount(0);
                            return newPlayerPersona;
                        });

                playerPersona.setCount(playerPersona.getCount() + 1);
                if (personaId != 68) {
                    playerPersonaRepository.save(playerPersona);
                }
                playerPersonaRepository.flush();

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
                rosterPersonaRepository.flush();
            }
        }
    }
}
