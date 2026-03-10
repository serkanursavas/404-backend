package com.squad.squad.service;

import com.squad.squad.context.GroupContext;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.mapper.RosterMapper;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.service.impl.RosterServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RosterServiceTest {

    @Mock private RosterRepository rosterRepository;
    @Mock private PlayerService playerService;
    @Mock private RosterMapper rosterMapper;
    @Mock private PlayerMapper playerMapper;

    private RosterServiceImpl rosterService;

    @BeforeEach
    void setUp() {
        rosterService = new RosterServiceImpl(rosterRepository, playerService, rosterMapper, playerMapper);
    }

    @AfterEach
    void clearContext() {
        GroupContext.clear();
    }

    @Test
    void updatePlayerGeneralRating_squad1Context_averagesOnlySquad1Rosters() {
        GroupContext.setCurrentGroupId(1);

        Player player = new Player();
        player.setId(10);

        Roster gameRoster = mock(Roster.class);
        when(gameRoster.getPlayer()).thenReturn(player);
        when(rosterRepository.findRosterByGameId(100)).thenReturn(List.of(gameRoster));

        Roster r1 = mock(Roster.class);
        Roster r2 = mock(Roster.class);
        when(r1.getRating()).thenReturn(7.0);
        when(r2.getRating()).thenReturn(8.0);
        when(rosterRepository.findRosterByPlayerIdAndSquadId(10, 1)).thenReturn(List.of(r1, r2));

        rosterService.updatePlayerGeneralRating(100);

        assertThat(player.getRating()).isEqualTo(7.5);
        verify(rosterRepository, never()).findRosterByPlayerIdAndSquadId(10, 2);
    }

    @Test
    void updatePlayerGeneralRating_squad2Context_averagesOnlySquad2Rosters() {
        GroupContext.setCurrentGroupId(2);

        Player player = new Player();
        player.setId(10);

        Roster gameRoster = mock(Roster.class);
        when(gameRoster.getPlayer()).thenReturn(player);
        when(rosterRepository.findRosterByGameId(100)).thenReturn(List.of(gameRoster));

        Roster r1 = mock(Roster.class);
        when(r1.getRating()).thenReturn(5.0);
        when(rosterRepository.findRosterByPlayerIdAndSquadId(10, 2)).thenReturn(List.of(r1));

        rosterService.updatePlayerGeneralRating(100);

        assertThat(player.getRating()).isEqualTo(5.0);
        verify(rosterRepository, never()).findRosterByPlayerIdAndSquadId(10, 1);
    }

    @Test
    void updatePlayerGeneralRating_noRostersForPlayer_setsRatingToZero() {
        GroupContext.setCurrentGroupId(1);

        Player player = new Player();
        player.setId(10);

        Roster gameRoster = mock(Roster.class);
        when(gameRoster.getPlayer()).thenReturn(player);
        when(rosterRepository.findRosterByGameId(100)).thenReturn(List.of(gameRoster));
        when(rosterRepository.findRosterByPlayerIdAndSquadId(10, 1)).thenReturn(List.of());

        rosterService.updatePlayerGeneralRating(100);

        assertThat(player.getRating()).isEqualTo(0.0);
    }
}
