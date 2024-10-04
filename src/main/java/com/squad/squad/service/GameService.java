package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;

public interface GameService {

    List<LatestGamesDTO> getAllGames();

    GameDTO getGameById(Integer id);

    GameDTO createGame(GameDTO gameDto);

    GameDTO updateGame(Integer id, GameDTO updatedGame);

    void updateScoreWithGoal(Goal goal);

    void deleteGame(Integer id);

    void checkIfVotingIsComplete(Integer game_id, String team_color);

    Game findById(Integer id);
}
