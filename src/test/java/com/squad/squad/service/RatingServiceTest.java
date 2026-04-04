package com.squad.squad.service;

import com.squad.squad.context.GroupContext;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.repository.RosterPersonaRepository;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.service.impl.RatingServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock private RatingRepository ratingRepository;
    @Mock private PlayerService playerService;
    @Mock private RosterService rosterService;
    @Mock private GameService gameService;
    @Mock private RosterPersonaRepository rosterPersonaRepository;
    @Mock private RosterRepository rosterRepository;
    @Mock private PlayerMapper playerMapper;
    @Mock private GroupAuthorizationService groupAuthorizationService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private RatingServiceImpl ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingServiceImpl(
                ratingRepository, playerService, rosterService, gameService,
                rosterPersonaRepository, rosterRepository, playerMapper, groupAuthorizationService, eventPublisher);
        ratingService.setRosterService(rosterService);
    }

    @AfterEach
    void clearContext() {
        GroupContext.clear();
    }

    @Test
    void checkVote_playerVotedInCurrentSquad_returnsTrue() {
        GroupContext.setCurrentGroupId(1);
        when(ratingRepository.existsByPlayerIdAndActiveGame(10, 1)).thenReturn(true);

        assertThat(ratingService.checkVote(10)).isTrue();
    }

    @Test
    void checkVote_playerNotVotedInCurrentSquad_returnsFalse() {
        GroupContext.setCurrentGroupId(1);
        when(ratingRepository.existsByPlayerIdAndActiveGame(10, 1)).thenReturn(false);

        assertThat(ratingService.checkVote(10)).isFalse();
    }

    @Test
    void checkVote_squad2ContextPlayerOnlyVotedInSquad1_returnsFalse() {
        // Player has voted in Squad1, but current context is Squad2
        GroupContext.setCurrentGroupId(2);
        when(ratingRepository.existsByPlayerIdAndActiveGame(10, 2)).thenReturn(false);

        assertThat(ratingService.checkVote(10)).isFalse();
        verify(ratingRepository, never()).existsByPlayerIdAndActiveGame(10, 1);
    }

    @Test
    void clearAllRatings_callsDeleteBySquadIdNotDeleteAll() {
        GroupContext.setCurrentGroupId(1);

        ratingService.clearAllRatings();

        verify(ratingRepository).deleteAllBySquadId(1);
        verify(ratingRepository, never()).deleteAll();
    }

    @Test
    void clearAllRatings_squad2Context_deletesOnlySquad2() {
        GroupContext.setCurrentGroupId(2);

        ratingService.clearAllRatings();

        verify(ratingRepository).deleteAllBySquadId(2);
        verify(ratingRepository, never()).deleteAllBySquadId(1);
    }
}
