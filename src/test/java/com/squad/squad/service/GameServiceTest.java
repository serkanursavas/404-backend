package com.squad.squad.service;

import com.squad.squad.context.GroupContext;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.GameLocation;
import com.squad.squad.entity.Squad;
import com.squad.squad.exception.GameNotFoundException;
import com.squad.squad.mapper.GameLocationMapper;
import com.squad.squad.mapper.GameMapper;
import com.squad.squad.mapper.GoalMapper;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.GameLocationRepository;
import com.squad.squad.repository.GameRepository;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.repository.RosterPersonaRepository;
import com.squad.squad.repository.SquadRepository;
import com.squad.squad.service.GroupAuthorizationService;
import com.squad.squad.service.impl.GameServiceImpl;
import org.springframework.context.ApplicationEventPublisher;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock private GameRepository gameRepository;
    @Mock private RosterService rosterService;
    @Mock private PlayerService playerService;
    @Mock private RosterPersonaRepository rosterPersonaRepository;
    @Mock private GameLocationRepository gameLocationRepository;
    @Mock private RatingRepository ratingRepository;
    @Mock private GoalMapper goalMapper;
    @Mock private GameMapper gameMapper;
    @Mock private GameLocationMapper gameLocationMapper;
    @Mock private PlayerMapper playerMapper;
    @Mock private SquadRepository squadRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private GroupAuthorizationService groupAuthorizationService;

    private GameServiceImpl gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameServiceImpl(
                gameRepository, rosterService, playerService, rosterPersonaRepository,
                gameLocationRepository, ratingRepository, goalMapper, gameMapper,
                gameLocationMapper, playerMapper, squadRepository, eventPublisher, groupAuthorizationService);
    }

    @AfterEach
    void clearContext() {
        GroupContext.clear();
    }

    // --- updateGame ---

    @Test
    void updateGame_gameFromDifferentSquad_throwsException() {
        GroupContext.setCurrentGroupId(1);
        when(gameRepository.findByIdAndSquadId(99, 1)).thenReturn(Optional.empty());

        GameUpdateRequestDTO dto = new GameUpdateRequestDTO();
        dto.setLocation("1");

        assertThrows(GameNotFoundException.class, () -> gameService.updateGame(99, dto));
        verify(gameRepository).findByIdAndSquadId(99, 1);
    }

    @Test
    void updateGame_gameBelongsToCurrentSquad_succeeds() {
        GroupContext.setCurrentGroupId(1);

        Game game = new Game();
        when(gameRepository.findByIdAndSquadId(1, 1)).thenReturn(Optional.of(game));

        GameLocation gameLocation = new GameLocation();
        when(gameLocationRepository.findById(1)).thenReturn(Optional.of(gameLocation));

        GameUpdateRequestDTO dto = new GameUpdateRequestDTO();
        dto.setLocation("1");
        dto.setRosters(List.of());

        assertDoesNotThrow(() -> gameService.updateGame(1, dto));
        verify(gameRepository).save(game);
    }

    // --- deleteGame ---

    @Test
    void deleteGame_gameFromDifferentSquad_throwsException() {
        GroupContext.setCurrentGroupId(1);
        when(gameRepository.findByIdAndSquadId(99, 1)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.deleteGame(99));
        verify(gameRepository).findByIdAndSquadId(99, 1);
        verify(gameRepository, never()).deleteById(99);
    }

    // --- updateWeather ---

    @Test
    void updateWeather_gameFromDifferentSquad_throwsException() {
        GroupContext.setCurrentGroupId(1);
        when(gameRepository.findByIdAndSquadId(99, 1)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.updateWeather(99, "SUNNY"));
        verify(gameRepository).findByIdAndSquadId(99, 1);
    }

    // --- getGameById (null guard fix) ---

    @Test
    void getGameById_squadIsNull_throwsSecurityException() {
        GroupContext.setCurrentGroupId(1);

        Game game = new Game();
        // game.squad intentionally left null — simulates a game with no squad association
        when(gameRepository.findById(99)).thenReturn(Optional.of(game));

        assertThrows(SecurityException.class, () -> gameService.getGameById(99));
    }

    @Test
    void getGameById_gameBelongsToDifferentSquad_throwsSecurityException() {
        GroupContext.setCurrentGroupId(1);

        Squad squad2 = new Squad();
        squad2.setId(2);

        Game game = new Game();
        game.setSquad(squad2);
        when(gameRepository.findById(99)).thenReturn(Optional.of(game));

        assertThrows(SecurityException.class, () -> gameService.getGameById(99));
    }
}
