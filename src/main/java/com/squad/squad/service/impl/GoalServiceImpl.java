package com.squad.squad.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.squad.squad.dto.TopListsDTO;
import com.squad.squad.dto.goal.AddGoalsRequestDTO;
import com.squad.squad.dto.goal.GoalAddRequestDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Player;
import com.squad.squad.mapper.GameMapper;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.GoalRepository;
import com.squad.squad.event.GoalScoredEvent;
import com.squad.squad.service.BaseSquadService;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GoalService;
import com.squad.squad.service.GroupAuthorizationService;
import com.squad.squad.service.PlayerService;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class GoalServiceImpl extends BaseSquadService implements GoalService {
    private final GoalRepository goalRepository;
    private final GameService gameService;
    private final PlayerService playerService;
    private final GameMapper gameMapper;
    private final PlayerMapper playerMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final GroupAuthorizationService groupAuthorizationService;

    @Autowired
    public GoalServiceImpl(GoalRepository goalRepository, GameService gameService, PlayerService playerService, GameMapper gameMapper, PlayerMapper playerMapper, ApplicationEventPublisher eventPublisher, GroupAuthorizationService groupAuthorizationService) {
        this.goalRepository = goalRepository;
        this.gameService = gameService;
        this.playerService = playerService;
        this.gameMapper = gameMapper;
        this.playerMapper = playerMapper;
        this.eventPublisher = eventPublisher;
        this.groupAuthorizationService = groupAuthorizationService;
    }

    @Override
    @Transactional
    public List<GoalDTO> getAllGoals() {
        Integer squadId = getSquadId();
        return goalRepository.findAllBySquadId(squadId).stream().map(
                        goal -> new GoalDTO(goal.getGame().getId(), goal.getPlayer().getId(), goal.getPlayer().getName(),
                                goal.getTeamColor()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GoalDTO> getGoalsByGameId(Integer gameId) {
        gameService.findGameById(gameId); // squad check — başka squad'ın maçı ise exception fırlatır
        return goalRepository.findGoalsByGameIdAndActiveTrue(gameId).stream().map(
                        goal -> new GoalDTO(gameId, goal.getPlayer().getId(), goal.getPlayer().getName(), goal.getTeamColor()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addGoals(AddGoalsRequestDTO requestDto) {
        Integer gameId = requestDto.getGameId();
        Game existingGame = gameService.findGameById(gameId);

        List<GoalAddRequestDTO> goalDtos = requestDto.getGoals();
        Goal lastSavedGoal = null;

        for (GoalAddRequestDTO goalDto : goalDtos) {
            Player existingPlayer = playerMapper.playerDTOToPlayer(playerService.getPlayerById(goalDto.getPlayerId()));

            Goal goal = new Goal();
            goal.setGame(existingGame);
            goal.setPlayer(existingPlayer);
            goal.setTeamColor(goalDto.getTeamColor());

            lastSavedGoal = goalRepository.save(goal);
        }

        // Skor artık tüm aktif gollerden tek seferde yeniden hesaplanıyor (delta artırımı yok,
        // bkz. GameServiceImpl.recalculateScore). isPlayed=true yan etkisi korunuyor.
        if (lastSavedGoal != null) {
            gameService.updateScoreWithGoal(lastSavedGoal);
        }

        Integer squadId = getSquadId();
        Integer actorUserId = groupAuthorizationService.getCurrentUserId();
        eventPublisher.publishEvent(new GoalScoredEvent(gameId, squadId, goalDtos.size(), actorUserId));
    }

    @Override
    @Transactional
    public void updateGoalsForGame(Integer gameId, List<GoalAddRequestDTO> desiredGoals) {
        Game game = gameService.findGameById(gameId); // squad check — başka squad'ın maçı ise exception fırlatır

        List<Goal> currentActiveGoals = goalRepository.findByGameIdAndActiveTrue(gameId);
        Map<Integer, List<Goal>> currentByPlayer = currentActiveGoals.stream()
                .collect(Collectors.groupingBy(g -> g.getPlayer().getId()));
        currentByPlayer.values().forEach(list -> list.sort(Comparator.comparing(Goal::getId)));

        Map<Integer, List<GoalAddRequestDTO>> desiredByPlayer = desiredGoals.stream()
                .collect(Collectors.groupingBy(GoalAddRequestDTO::getPlayerId));

        Set<Integer> allPlayerIds = new HashSet<>();
        allPlayerIds.addAll(currentByPlayer.keySet());
        allPlayerIds.addAll(desiredByPlayer.keySet());

        for (Integer playerId : allPlayerIds) {
            List<Goal> existing = currentByPlayer.getOrDefault(playerId, new ArrayList<>());
            List<GoalAddRequestDTO> desired = desiredByPlayer.getOrDefault(playerId, new ArrayList<>());

            if (desired.size() < existing.size()) {
                // Fazla olan golleri en yeniden (en yüksek id) başlayarak soft-delete et.
                // Dokunulmayan goller korunur, Envers geçmişi gereksiz şişmez.
                int toRemove = existing.size() - desired.size();
                for (int i = 0; i < toRemove; i++) {
                    Goal goalToRemove = existing.get(existing.size() - 1 - i);
                    goalToRemove.setActive(false);
                    goalRepository.save(goalToRemove);
                }
            } else if (desired.size() > existing.size()) {
                int toAdd = desired.size() - existing.size();
                Player player = playerMapper.playerDTOToPlayer(playerService.getPlayerById(playerId));
                for (int i = 0; i < toAdd; i++) {
                    String teamColor = desired.get(existing.size() + i).getTeamColor();
                    Goal newGoal = new Goal();
                    newGoal.setGame(game);
                    newGoal.setPlayer(player);
                    newGoal.setTeamColor(teamColor);
                    goalRepository.save(newGoal);
                }
            }
            // Eşitse dokunma.
        }

        // isPlayed'a dokunulmuyor — eski bir maçı düzenlemek durumunu değiştirmemeli.
        gameService.recalculateScore(gameId);
    }

    public List<TopListsDTO> getTopScorers() {
        Integer squadId = getSquadId();
        List<Object[]> results = goalRepository.findTopScorersNative(squadId);
        List<TopListsDTO> topScorers = new ArrayList<>();

        for (Object[] result : results) {
            Integer playerId = (Integer) result[0];
            String name = (String) result[1];
            String surname = (String) result[2];
            String position = (String) result[3];
            Long goalCount = ((Number) result[4]).longValue();
            Long gameCount = ((Number) result[5]).longValue();

            TopListsDTO dto = new TopListsDTO(playerId, name, surname, goalCount, gameCount);
            dto.setPosition(position);
            topScorers.add(dto);
        }

        return topScorers;
    }
}
