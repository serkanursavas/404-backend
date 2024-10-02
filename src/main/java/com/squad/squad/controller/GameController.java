package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.GamesDTO;
import com.squad.squad.dto.GoalDTO;
import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GoalService;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RosterService;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/games")

public class GameController {

    private final GameService gameService;
    private final PlayerService playerService;
    private final RosterService rosterService;
    private final GoalService goalService;

    public GameController(GameService gameService, PlayerService playerService, RosterService rosterService,
            GoalService goalService) {
        this.gameService = gameService;
        this.playerService = playerService;
        this.rosterService = rosterService;
        this.goalService = goalService;
    }

    @GetMapping("")
    public List<GamesDTO> getAllGames() {
        return gameService.getAllGames();
    }

    @GetMapping("/{id}")
    public GameDTO getGameById(@PathVariable Integer id) {
        Game game = gameService.getGameById(id);
        List<RosterDTO> rosters = rosterService.findRosterByGameId(id);

        for (RosterDTO rosterDTO : rosters) {
            Player player = playerService.getPlayerById(rosterDTO.getPlayerId());
            rosterDTO.setPlayerName(player.getName() + " " + player.getSurname());
        }

        List<GoalDTO> goals = goalService.getGoalsByGameId(id);

        for (GoalDTO goalDTO : goals) {
            Player player = playerService.getPlayerById(goalDTO.getPlayer_id());
            goalDTO.setPlayer_name(player.getName());
        }

        GameDTO gameDTO = new GameDTO();
        BeanUtils.copyProperties(game, gameDTO);
        gameDTO.setRosters(rosters);
        gameDTO.setGoals(goals);

        return gameDTO;

    }

    @PostMapping("")
    public ResponseEntity<String> saveGame(@RequestBody GameDTO gameDTO) {

        if (gameDTO.getRosters() == null || gameDTO.getRosters().size() != gameDTO.getTeamSize() * 2) {
            return ResponseEntity.badRequest()
                    .body("Roster list must contain " + gameDTO.getTeamSize() * 2 + " players");
        }

        Set<Integer> playerIds = new HashSet<>();

        for (RosterDTO roster : gameDTO.getRosters()) {
            if (!playerIds.add(roster.getPlayerId())) {
                return ResponseEntity.badRequest().body("Duplicate playerId found: " + roster.getPlayerId());
            }
        }

        Game game = new Game();
        game.setDateTime(gameDTO.getDateTime());
        game.setWeather(gameDTO.getWeather());
        game.setLocation(gameDTO.getLocation());
        game.setHomeTeamScore(gameDTO.getHomeTeamScore());
        game.setAwayTeamScore(gameDTO.getAwayTeamScore());

        List<Roster> rosters = new ArrayList<>();

        for (RosterDTO rosterDTO : gameDTO.getRosters()) {
            Roster roster = new Roster();
            roster.setGame(game);
            roster.setTeamColor(rosterDTO.getTeamColor());

            Player player = playerService.getPlayerById(rosterDTO.getPlayerId());

            if (player == null) {
                throw new RuntimeException("Player not found with id: " + rosterDTO.getPlayerId());
            }

            roster.setPlayer(player);

            rosters.add(roster);
        }

        gameService.createGameWithRoster(game, rosters);

        return ResponseEntity.ok("game saveds");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateGame(@PathVariable Integer id, @RequestBody Game updatedGame) {

        gameService.updateGame(id, updatedGame);
        return ResponseEntity.ok("game updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGame(@PathVariable Integer id) {
        gameService.deleteGame(id);
        return ResponseEntity.ok("game deleted");
    }

}
