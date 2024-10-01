package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RestController;

import com.squad.squad.dto.GameDTO;
import com.squad.squad.dto.GamesDTO;
import com.squad.squad.dto.GoalDTO;
import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Game;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.repository.PlayerRepository;
import com.squad.squad.service.GameService;
import com.squad.squad.service.GoalService;
import com.squad.squad.service.RosterService;

import java.util.ArrayList;
import java.util.List;

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
    private final PlayerRepository playerRepository;
    private final RosterService rosterService;
    private final GoalService goalService;

    public GameController(GameService gameService, PlayerRepository playerRepository, RosterService rosterService,
            GoalService goalService) {
        this.gameService = gameService;
        this.playerRepository = playerRepository;
        this.rosterService = rosterService;
        this.goalService = goalService;
    }

    @GetMapping("")
    public List<GamesDTO> getAllGames() {
        return gameService.getAllGames();
    }

    @GetMapping("/{id}")
    public GameDTO getGameById(@PathVariable Integer id) {

        // 1. Game entity'sini GameService üzerinden alıyoruz
        Game game = gameService.getGameById(id);

        // 2. Game ID'ye göre ilgili Roster entity'lerini alıyoruz
        List<RosterDTO> rosters = rosterService.findRosterByGameId(id);

        for (RosterDTO rosterDTO : rosters) {
            Player player = playerRepository.findById(rosterDTO.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found with id: " + rosterDTO.getPlayerId()));
            rosterDTO.setPlayerName(player.getName() + " " + player.getSurname());
        }

        List<GoalDTO> goals = goalService.getGoalsByGameId(id);

        for (GoalDTO goalDTO : goals) {
            Player player = playerRepository.findById(goalDTO.getPlayer_id())
                    .orElseThrow(() -> new RuntimeException("Player not found with id: " + goalDTO.getGame_id()));
            goalDTO.setPlayer_name(player.getName());
        }

        // 3. GameDTO oluşturuyoruz ve alanları dolduruyoruz
        GameDTO gameDTO = new GameDTO();
        gameDTO.setId(game.getId());
        gameDTO.setLocation(game.getLocation());
        gameDTO.setWeather(game.getWeather());
        gameDTO.setDateTime(game.getDateTime());
        gameDTO.setHomeTeamScore(game.getHomeTeamScore());
        gameDTO.setAwayTeamScore(game.getAwayTeamScore());

        // 4. Roster listesi DTO'ya ekleniyor
        gameDTO.setRosters(rosters);
        gameDTO.setGoals(goals);

        return gameDTO; // Doldurulmuş GameDTO döndürülüyor

    }

    @PostMapping("")
    public ResponseEntity<String> saveGame(@RequestBody GameDTO gameDTO) {

        Game game = new Game();
        game.setDateTime(gameDTO.getDateTime());
        game.setWeather(gameDTO.getWeather());
        game.setLocation(gameDTO.getLocation());
        game.setHomeTeamScore(gameDTO.getHomeTeamScore());
        game.setAwayTeamScore(gameDTO.getAwayTeamScore());

        // Oyuncuları roster'a ekleme
        List<Roster> rosters = new ArrayList<>();

        for (RosterDTO rosterDTO : gameDTO.getRosters()) {
            Roster roster = new Roster();
            roster.setGame(game);
            roster.setTeamColor(rosterDTO.getTeamColor());

            // Player entity'sini veritabanından bulup ilişkilendirin
            Player player = playerRepository.findById(rosterDTO.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found with id: " + rosterDTO.getPlayerId()));
            roster.setPlayer(player); // Player ile ilişkilendirilir

            rosters.add(roster);
        }

        gameService.createGameWithRoster(game, rosters);

        return ResponseEntity.ok("game saved");
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
