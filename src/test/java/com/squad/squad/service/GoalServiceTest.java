package com.squad.squad.service;

import com.squad.squad.context.GroupContext;
import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Player;
import com.squad.squad.exception.GameNotFoundException;
import com.squad.squad.mapper.GameMapper;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.GoalRepository;
import com.squad.squad.service.impl.GoalServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock private GoalRepository goalRepository;
    @Mock private GameService gameService;
    @Mock private PlayerService playerService;
    @Mock private GameMapper gameMapper;
    @Mock private PlayerMapper playerMapper;

    private GoalServiceImpl goalService;

    @BeforeEach
    void setUp() {
        goalService = new GoalServiceImpl(goalRepository, gameService, playerService, gameMapper, playerMapper);
    }

    @AfterEach
    void clearContext() {
        GroupContext.clear();
    }

    // --- getAllGoals ---

    @Test
    void getAllGoals_returnsOnlyCurrentSquadGoals() {
        GroupContext.setCurrentGroupId(1);

        Goal goal1 = mock(Goal.class);
        Goal goal2 = mock(Goal.class);

        Game game = mock(Game.class);
        when(game.getId()).thenReturn(10);

        Player player1 = mock(Player.class);
        when(player1.getId()).thenReturn(5);
        when(player1.getName()).thenReturn("Ali");

        Player player2 = mock(Player.class);
        when(player2.getId()).thenReturn(6);
        when(player2.getName()).thenReturn("Mehmet");

        when(goal1.getGame()).thenReturn(game);
        when(goal1.getPlayer()).thenReturn(player1);
        when(goal1.getTeamColor()).thenReturn("BLACK");

        when(goal2.getGame()).thenReturn(game);
        when(goal2.getPlayer()).thenReturn(player2);
        when(goal2.getTeamColor()).thenReturn("WHITE");

        when(goalRepository.findAllBySquadId(1)).thenReturn(List.of(goal1, goal2));

        List<GoalDTO> result = goalService.getAllGoals();

        assertThat(result).hasSize(2);
        verify(goalRepository).findAllBySquadId(1);
        verify(goalRepository, never()).findAll();
    }

    // --- getGoalsByGameId ---

    @Test
    void getGoalsByGameId_gameFromDifferentSquad_throwsException() {
        GroupContext.setCurrentGroupId(1);

        when(gameService.findGameById(99))
                .thenThrow(new GameNotFoundException("Game not found with id: 99"));

        assertThrows(GameNotFoundException.class, () -> goalService.getGoalsByGameId(99));
        verify(goalRepository, never()).findGoalsByGameId(99);
    }

    @Test
    void getGoalsByGameId_validGame_returnsGoals() {
        GroupContext.setCurrentGroupId(1);

        Game game = new Game();
        when(gameService.findGameById(1)).thenReturn(game);

        Goal goal = mock(Goal.class);
        Player player = mock(Player.class);
        when(player.getId()).thenReturn(5);
        when(player.getName()).thenReturn("Ali");
        when(goal.getPlayer()).thenReturn(player);
        when(goal.getTeamColor()).thenReturn("BLACK");

        when(goalRepository.findGoalsByGameId(1)).thenReturn(List.of(goal));

        List<GoalDTO> result = goalService.getGoalsByGameId(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlayerId()).isEqualTo(5);
        verify(gameService).findGameById(1);
        verify(goalRepository).findGoalsByGameId(1);
    }
}
