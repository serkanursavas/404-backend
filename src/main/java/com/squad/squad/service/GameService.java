package com.squad.squad.service;

import java.util.List;
import java.util.Optional;

import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.dto.MvpDTO;
import com.squad.squad.dto.game.GameCreateRequestDTO;
import com.squad.squad.dto.game.GameResponseDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Roster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GameService {

    Page<LatestGamesDTO> getAllGames();

    GameResponseDTO getGameById(Integer id);

    Page<LatestGamesDTO> getAllGames(Pageable pageable);

    Game findGameById(Integer id);

    void createGame(GameCreateRequestDTO gameDto);

    void updateGame(Integer id, GameUpdateRequestDTO updatedGame);

    void updateScoreWithGoal(Goal goal);

    void deleteGame(Integer id);

    Game findById(Integer id);

    GameResponseDTO getLatestGame();

    void checkAndUpdateUnplayedGame();

    List<Roster> getRostersByGameId(Integer gameId);

    void updateVote(Game game);

    Optional<MvpDTO> getMvpPlayer();
}