package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.GamesDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Roster;

public interface GameService {

    List<GamesDTO> getAllGames();

    Game getGameById(Integer id);

    Game createGameWithRoster(Game game, List<Roster> rosters);

    Game updateGame(Integer id, Game updatedGame);

    void updateScoreWithGoal(Goal goal);

    void deleteGame(Integer id);

    void checkIfVotingIsComplete(Integer game_id, String team_color);

}
