package com.squad.squad.service.impl;

import com.squad.squad.dto.AddPersonaRequestDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Persona;
import com.squad.squad.entity.PlayerPersona;
import com.squad.squad.entity.Roster;
import com.squad.squad.entity.RosterPersona;
import com.squad.squad.entity.Squad;
import com.squad.squad.repository.PersonaRepository;
import com.squad.squad.repository.PlayerPersonaRepository;
import com.squad.squad.repository.RosterPersonaRepository;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.repository.SquadRepository;
import com.squad.squad.service.BaseSquadService;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GroupAuthorizationService;
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
    private final RosterRepository rosterRepository;
    private final GameService gameService;
    private final GroupAuthorizationService authService;

    public PersonaServiceImpl(PersonaRepository personaRepository,
                              RosterService rosterService,
                              PlayerPersonaRepository playerPersonaRepository,
                              RosterPersonaRepository rosterPersonaRepository,
                              SquadRepository squadRepository,
                              RosterRepository rosterRepository,
                              GameService gameService,
                              GroupAuthorizationService authService) {
        this.personaRepository = personaRepository;
        this.rosterService = rosterService;
        this.playerPersonaRepository = playerPersonaRepository;
        this.rosterPersonaRepository = rosterPersonaRepository;
        this.squadRepository = squadRepository;
        this.rosterRepository = rosterRepository;
        this.gameService = gameService;
        this.authService = authService;
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

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void resubmitPersonasForGame(Integer gameId, List<AddPersonaRequestDTO> personas) {
        Integer userId = authService.getCurrentUserId();
        Integer playerId = authService.getCurrentPlayerId();
        Integer squadId = getSquadId();

        if (rosterPersonaRepository.existsByGameIdAndCreatedByUserId(gameId, userId)) {
            throw new IllegalStateException("You have already submitted personas for this game.");
        }

        Roster voterRoster = rosterService.getRosterByPlayerIdAndGameId(gameId, playerId);
        if (voterRoster == null) {
            throw new IllegalArgumentException("You are not a participant in this game.");
        }

        if (!voterRoster.getHasVote()) {
            throw new IllegalStateException("You must have submitted ratings to resubmit personas.");
        }

        Squad squad = squadRepository.findById(squadId).orElse(null);

        for (AddPersonaRequestDTO dto : personas) {
            Roster existingRoster = rosterService.getRosterById(dto.getRosterId());

            if (dto.getPersonaIds().size() > 3) {
                throw new IllegalArgumentException("Roster ID " + dto.getRosterId() + " has more than 3 personas.");
            }

            for (Integer personaId : dto.getPersonaIds()) {
                Persona persona = personaRepository.findById(personaId)
                        .orElseThrow(() -> new IllegalArgumentException("Persona not found: " + personaId));

                if (personaId != 68) {
                    PlayerPersona playerPersona = playerPersonaRepository
                            .findByPlayerIdAndPersonaIdAndSquadId(existingRoster.getPlayer().getId(), persona.getId(), squadId)
                            .orElseGet(() -> {
                                PlayerPersona pp = new PlayerPersona();
                                pp.setPlayer(existingRoster.getPlayer());
                                pp.setPersona(persona);
                                pp.setSquad(squad);
                                pp.setCount(0);
                                return pp;
                            });
                    playerPersona.setCount(playerPersona.getCount() + 1);
                    playerPersonaRepository.save(playerPersona);
                }

                RosterPersona rosterPersona = rosterPersonaRepository
                        .findByRosterIdAndPersonaId(existingRoster.getId(), persona.getId())
                        .orElseGet(() -> {
                            RosterPersona rp = new RosterPersona();
                            rp.setRoster(existingRoster);
                            rp.setPersona(persona);
                            rp.setCount(0);
                            return rp;
                        });
                rosterPersona.setCount(rosterPersona.getCount() + 1);
                rosterPersonaRepository.save(rosterPersona);
            }
        }
        rosterPersonaRepository.flush();

        int totalRosters = rosterRepository.findAllByGameId(gameId).size();
        Integer submitters = rosterPersonaRepository.countDistinctSubmittersByGameId(gameId);
        if (submitters != null && submitters >= totalRosters) {
            recalculatePersonasForGame(gameId);
        }
    }

    @Override
    @Transactional
    public void recalculatePersonasForGame(Integer gameId) {
        List<Roster> rosters = rosterRepository.findAllByGameId(gameId);

        for (Roster roster : rosters) {
            List<RosterPersona> top3 = rosterPersonaRepository.findTop3ByRosterId(roster.getId());
            roster.setPersona1(!top3.isEmpty() ? top3.get(0).getPersona().getId() : null);
            roster.setPersona2(top3.size() > 1 ? top3.get(1).getPersona().getId() : null);
            roster.setPersona3(top3.size() > 2 ? top3.get(2).getPersona().getId() : null);
        }

        rosterService.updateAllRosters(rosters);

        Integer squadId = getSquadId();
        Integer mvpId = rosterPersonaRepository.findMvp(squadId);
        Game game = gameService.findGameById(gameId);
        game.setMvpId(mvpId);
        game.setVoted(true);
        gameService.updateVote(game);
    }
}
