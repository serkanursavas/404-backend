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

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void savePersonas(Integer gameId, List<AddPersonaRequestDTO> personas) {
        Integer playerId = authService.getCurrentPlayerId();
        Integer squadId = getSquadId();

        Roster voterRoster = rosterService.getRosterByPlayerIdAndGameId(gameId, playerId);
        if (voterRoster == null) {
            throw new IllegalArgumentException("You are not a participant in this game.");
        }
        if (Boolean.TRUE.equals(voterRoster.getHasPersonaVote())) {
            throw new IllegalStateException("You have already submitted personas for this game.");
        }

        Squad squad = squadRepository.findById(squadId).orElse(null);

        for (AddPersonaRequestDTO dto : personas) {
            Roster existingRoster = rosterService.getRosterById(dto.getRosterId());

            if (dto.getPersonaIds() == null) continue;

            if (dto.getPersonaIds().size() > 3) {
                throw new IllegalArgumentException("Max 3 personas per player.");
            }

            for (Integer personaId : dto.getPersonaIds()) {
                Persona persona = personaRepository.findById(personaId)
                        .orElseThrow(() -> new IllegalArgumentException("Persona not found with ID: " + personaId));

                if (personaId != 68) {
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
                    playerPersonaRepository.save(playerPersona);
                }

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

        rosterRepository.setHasPersonaVoteTrue(voterRoster.getId());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void resubmitPersonasForGame(Integer gameId, List<AddPersonaRequestDTO> personas) {
        Integer playerId = authService.getCurrentPlayerId();
        Integer squadId = getSquadId();

        Roster voterRoster = rosterService.getRosterByPlayerIdAndGameId(gameId, playerId);
        if (voterRoster == null) {
            throw new IllegalArgumentException("You are not a participant in this game.");
        }
        if (Boolean.TRUE.equals(voterRoster.getHasPersonaVote())) {
            throw new IllegalStateException("You have already submitted personas for this game.");
        }

        Squad squad = squadRepository.findById(squadId).orElse(null);

        for (AddPersonaRequestDTO dto : personas) {
            Roster targetRoster = rosterService.getRosterById(dto.getRosterId());

            if (dto.getPersonaIds() != null && dto.getPersonaIds().size() > 3) {
                throw new IllegalArgumentException("Max 3 personas per player.");
            }

            if (dto.getPersonaIds() == null) continue;

            for (Integer personaId : dto.getPersonaIds()) {
                Persona persona = personaRepository.findById(personaId)
                        .orElseThrow(() -> new IllegalArgumentException("Persona not found: " + personaId));

                if (personaId != 68) {
                    PlayerPersona pp = playerPersonaRepository
                            .findByPlayerIdAndPersonaIdAndSquadId(targetRoster.getPlayer().getId(), personaId, squadId)
                            .orElseGet(() -> {
                                PlayerPersona newPp = new PlayerPersona();
                                newPp.setPlayer(targetRoster.getPlayer());
                                newPp.setPersona(persona);
                                newPp.setSquad(squad);
                                newPp.setCount(0);
                                return newPp;
                            });
                    pp.setCount(pp.getCount() + 1);
                    playerPersonaRepository.save(pp);
                }

                RosterPersona rp = new RosterPersona();
                rp.setRoster(targetRoster);
                rp.setPersona(persona);
                rp.setCount(1);
                rosterPersonaRepository.save(rp);
            }
        }

        voterRoster.setHasPersonaVote(true);
        rosterRepository.save(voterRoster);

        long total = rosterRepository.countByGameId(gameId);
        long pressed = rosterRepository.countByGameIdAndHasPersonaVoteTrue(gameId);
        if (pressed >= total) {
            recalculatePersonasForGame(gameId);
        }
    }

    @Override
    @Transactional
    public void recalculatePersonasForGame(Integer gameId) {
        List<Roster> rosters = rosterRepository.findAllByGameId(gameId);

        for (Roster roster : rosters) {
            List<Integer> top3 = rosterPersonaRepository.findTop3PersonaIdsByRosterId(roster.getId());
            roster.setPersona1(!top3.isEmpty() ? top3.get(0) : null);
            roster.setPersona2(top3.size() > 1 ? top3.get(1) : null);
            roster.setPersona3(top3.size() > 2 ? top3.get(2) : null);
        }

        rosterService.updateAllRosters(rosters);

        Integer squadId = getSquadId();
        Integer mvpId = rosterPersonaRepository.findMvpGrouped(squadId);
        Game game = gameService.findGameById(gameId);
        game.setMvpId(mvpId);
        game.setVoted(true);
        gameService.updateVote(game);
    }
}
