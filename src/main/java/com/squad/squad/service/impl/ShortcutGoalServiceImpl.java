package com.squad.squad.service.impl;

import com.squad.squad.entity.Game;
import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import com.squad.squad.enums.TeamColor;
import com.squad.squad.repository.GameRepository;
import com.squad.squad.repository.GoalRepository;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.service.BaseSquadService;
import com.squad.squad.service.ShortcutGoalService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Apple Shortcuts ile canlı gol girişi iş mantığı. Bilerek GoalServiceImpl/GameServiceImpl'in
 * mevcut addGoals/updateScoreWithGoal akışını kullanmaz: o akış ilk golde maçı isPlayed=true
 * yapıyor, bu da ikinci golde "aktif maç" aramasını kırar. Burada isPlayed'a sadece
 * finishMatch() dokunur — maçın yaşam döngüsünü Shortcut ("maçı bitir") kontrol eder.
 */
@Service
public class ShortcutGoalServiceImpl extends BaseSquadService implements ShortcutGoalService {

    private final GameRepository gameRepository;
    private final GoalRepository goalRepository;
    private final RosterRepository rosterRepository;
    private final PlayerNameMatcher playerNameMatcher;

    public ShortcutGoalServiceImpl(GameRepository gameRepository, GoalRepository goalRepository,
            RosterRepository rosterRepository, PlayerNameMatcher playerNameMatcher) {
        this.gameRepository = gameRepository;
        this.goalRepository = goalRepository;
        this.rosterRepository = rosterRepository;
        this.playerNameMatcher = playerNameMatcher;
    }

    @Override
    @Transactional
    public String addGoalByPlayerName(String dictatedName) {
        Game game = resolveActiveGame();
        if (game == null) {
            return "Aktif maç yok.";
        }

        List<Roster> rosters = rosterRepository.findRosterByGameId(game.getId());
        if (rosters.isEmpty()) {
            return "Aktif maçta kadro bulunamadı.";
        }

        Optional<Roster> matched = playerNameMatcher.match(dictatedName, rosters);
        if (matched.isEmpty()) {
            return "Oyuncu bulunamadı: " + dictatedName;
        }

        Roster roster = matched.get();
        Player player = roster.getPlayer();
        TeamColor teamColor = TeamColor.fromString(roster.getTeamColor());

        Goal goal = new Goal();
        goal.setGame(game);
        goal.setPlayer(player);
        goal.setTeamColor(teamColor.name());
        goalRepository.save(goal);

        applyScoreDelta(game, teamColor, 1);
        gameRepository.save(game);

        return String.format("Gol! %s — %s. Skor %d-%d.",
                fullName(player), turkishColor(teamColor), game.getHomeTeamScore(), game.getAwayTeamScore());
    }

    @Override
    @Transactional
    public String undoLastGoal() {
        Game game = resolveActiveGame();
        if (game == null) {
            return "Aktif maç yok.";
        }

        Optional<Goal> lastGoal = goalRepository.findTopByGame_IdAndActiveTrueOrderByIdDesc(game.getId());
        if (lastGoal.isEmpty()) {
            return "Geri alınacak gol yok.";
        }

        Goal goal = lastGoal.get();
        goal.setActive(false);
        goalRepository.save(goal);

        TeamColor teamColor = TeamColor.fromString(goal.getTeamColor());
        applyScoreDelta(game, teamColor, -1);
        gameRepository.save(game);

        return String.format("Son gol geri alındı: %s. Skor %d-%d.",
                fullName(goal.getPlayer()), game.getHomeTeamScore(), game.getAwayTeamScore());
    }

    @Override
    public String getScoreText() {
        Game game = resolveActiveGame();
        if (game == null) {
            return "Aktif maç yok.";
        }
        return String.format("Skor %d-%d.", game.getHomeTeamScore(), game.getAwayTeamScore());
    }

    @Override
    @Transactional
    public String finishMatch() {
        Game game = resolveActiveGame();
        if (game == null) {
            return "Aktif maç yok.";
        }
        game.setPlayed(true);
        gameRepository.save(game);
        return String.format("Maç kapatıldı. Final skor %d-%d.", game.getHomeTeamScore(), game.getAwayTeamScore());
    }

    private Game resolveActiveGame() {
        Integer squadId = getSquadId();
        return gameRepository.findBySquadIdAndIsPlayedFalse(squadId);
    }

    private void applyScoreDelta(Game game, TeamColor teamColor, int delta) {
        if (teamColor == TeamColor.BLACK) {
            game.setHomeTeamScore(Math.max(0, game.getHomeTeamScore() + delta));
        } else {
            game.setAwayTeamScore(Math.max(0, game.getAwayTeamScore() + delta));
        }
    }

    private String fullName(Player player) {
        String name = player.getName() == null ? "" : player.getName();
        String surname = player.getSurname() == null ? "" : player.getSurname();
        return (name + " " + surname).trim();
    }

    private String turkishColor(TeamColor teamColor) {
        return teamColor == TeamColor.BLACK ? "Siyah" : "Beyaz";
    }
}
