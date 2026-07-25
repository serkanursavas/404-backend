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
import com.squad.squad.service.GameService;
import com.squad.squad.service.ShortcutGoalService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Apple Shortcuts ile canlı gol girişi iş mantığı. Bilerek GoalServiceImpl/GameServiceImpl'in
 * mevcut addGoals/updateScoreWithGoal akışını kullanmaz: o akış golde maçı isPlayed=true yapar,
 * ama burada "isPlayed" hiç kullanılmıyor — mevcut sistemde isPlayed gerçekte "başlama saati
 * geçti" anlamına geliyor (bkz. GameServiceImpl.checkAndUpdateUnplayedGame), "maç bitti" değil.
 * Canlı maç burada isVoted=false + kick-off'tan itibaren bir zaman penceresi ile çözülür.
 */
@Service
public class ShortcutGoalServiceImpl extends BaseSquadService implements ShortcutGoalService {

    /** Gol girişinin kick-off'tan itibaren açık kaldığı süre. */
    private static final Duration LIVE_WINDOW = Duration.ofMinutes(90);

    private final GameRepository gameRepository;
    private final GoalRepository goalRepository;
    private final RosterRepository rosterRepository;
    private final PlayerNameMatcher playerNameMatcher;
    private final GameService gameService;

    public ShortcutGoalServiceImpl(GameRepository gameRepository, GoalRepository goalRepository,
            RosterRepository rosterRepository, PlayerNameMatcher playerNameMatcher, GameService gameService) {
        this.gameRepository = gameRepository;
        this.goalRepository = goalRepository;
        this.rosterRepository = rosterRepository;
        this.playerNameMatcher = playerNameMatcher;
        this.gameService = gameService;
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

        Game updatedGame = gameService.recalculateScore(game.getId());

        return String.format("Gol! %s — %s. Skor %d-%d.",
                fullName(player), turkishColor(teamColor), updatedGame.getHomeTeamScore(), updatedGame.getAwayTeamScore());
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

        Game updatedGame = gameService.recalculateScore(game.getId());

        return String.format("Son gol geri alındı: %s. Skor %d-%d.",
                fullName(goal.getPlayer()), updatedGame.getHomeTeamScore(), updatedGame.getAwayTeamScore());
    }

    @Override
    public String getScoreText() {
        Game game = resolveActiveGame();
        if (game == null) {
            return "Aktif maç yok.";
        }
        return String.format("Skor %d-%d.", game.getHomeTeamScore(), game.getAwayTeamScore());
    }

    private Game resolveActiveGame() {
        Integer squadId = getSquadId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minus(LIVE_WINDOW);
        List<Game> candidates = gameRepository.findLiveCandidates(squadId, windowStart, now);
        return candidates.isEmpty() ? null : candidates.get(0);
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
