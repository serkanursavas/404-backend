package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.dto.game.GameCreateRequestDTO;
import com.squad.squad.dto.game.GameResponseDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.dto.game.NextGameResponseDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Roster;

public interface GameService {

    List<LatestGamesDTO> getAllGames();

    GameResponseDTO getGameById(Integer id);

    Game findGameById(Integer id);

    void createGame(GameCreateRequestDTO gameDto);

    void updateGame(Integer id, GameUpdateRequestDTO updatedGame);

    void updateScoreWithGoal(Goal goal);

    void deleteGame(Integer id);

    Game findById(Integer id);

    NextGameResponseDTO getLatestGame();

    void checkAndUpdateUnplayedGame();

    List<Roster> getRostersByGameId(Integer gameId);
}