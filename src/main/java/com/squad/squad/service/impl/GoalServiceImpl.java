package com.squad.squad.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.squad.squad.dto.TopListsDTO;
import com.squad.squad.dto.goal.AddGoalsRequestDTO;
import com.squad.squad.dto.goal.GoalAddRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Player;
import com.squad.squad.mapper.GameMapper;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.GoalRepository;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GoalService;
import com.squad.squad.service.PlayerService;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final GameService gameService;
    private final PlayerService playerService;
    private final GameMapper gameMapper;
    private final PlayerMapper playerMapper;

    @Autowired
    public GoalServiceImpl(GoalRepository goalRepository, GameService gameService, PlayerService playerService, GameMapper gameMapper, PlayerMapper playerMapper) {
        this.goalRepository = goalRepository;
        this.gameService = gameService;
        this.playerService = playerService;
        this.gameMapper = gameMapper;
        this.playerMapper = playerMapper;
    }

    @Override
    public List<GoalDTO> getAllGoals() {
        return goalRepository.findAll().stream().map(
                        goal -> new GoalDTO(goal.getGame().getId(), goal.getPlayer().getId(), goal.getPlayer().getName(),
                                goal.getTeamColor()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GoalDTO> getGoalsByGameId(Integer gameId) {
        return goalRepository.findGoalsByGameId(gameId).stream().map(
                        goal -> new GoalDTO(gameId, goal.getPlayer().getId(), goal.getPlayer().getName(), goal.getTeamColor()))
                .collect(Collectors.toList());
    }

    @Override
    public void addGoals(AddGoalsRequestDTO requestDto) {

        Integer gameId = requestDto.getGameId();
        Game existingGame = gameService.findGameById(gameId);

        List<GoalAddRequestDTO> goalDtos = requestDto.getGoals();

        for (GoalAddRequestDTO goalDto : goalDtos) {

            Player existingPlayer = playerMapper.playerDTOToPlayer(playerService.getPlayerById(goalDto.getPlayerId()));

            Goal goal = new Goal();
            goal.setGame(existingGame);
            goal.setPlayer(existingPlayer);
            goal.setTeamColor(goalDto.getTeamColor());

            goalRepository.save(goal);

            gameService.updateScoreWithGoal(goal);
        }
    }

    public List<TopListsDTO> getTopScorers() {
        List<Object[]> results = goalRepository.findTopScorersNative();
        List<TopListsDTO> topScorers = new ArrayList<>();

        for (Object[] result : results) {
            Integer playerId = (Integer) result[0];
            String name = (String) result[1];
            String surname = (String) result[2];
            Long goalCount = ((Number) result[3]).longValue(); // Long'a dönüştürüyoruz

            TopListsDTO dto = new TopListsDTO(playerId, name, surname, goalCount);
            topScorers.add(dto);
        }

        return topScorers;
    }
}