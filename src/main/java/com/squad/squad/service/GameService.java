package com.squad.squad.service;

import com.squad.squad.dto.GamesDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Roster;
import jakarta.transaction.Transactional;

import java.util.List;

public interface GameService {
    List<GamesDTO> getAllGames();

    Game getGameById(Integer id);

    @Transactional
    Game createGameWithRoster(Game game, List<Roster> rosters);

    Game updateGame(Integer id, Game updatedGame);

    @Transactional
    void deleteGame(Integer id);

    void checkIfVotingIsComplete(Integer game_id, String team_color);

    void updateScoreWithGoal(Goal goal);
}
