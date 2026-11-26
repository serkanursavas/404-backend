package com.squad.squad.service.impl;

import com.squad.squad.entity.Game;
import com.squad.squad.entity.GameNotificationState;
import com.squad.squad.entity.GroupMembership;
import com.squad.squad.entity.Roster;
import com.squad.squad.repository.GameNotificationStateRepository;
import com.squad.squad.repository.GameRepository;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.repository.RosterRepository;
import com.squad.squad.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maç sonrası bildirim takvimi (kullanıcı kararı 26 Kas 2026). Backend'de maç bitişi ve oylama son tarihi yok;
 * takvim başlangıç saatinden (B) sayılır, maç 60 dk varsayılır. Oylama kapanınca (herkes oy verince) durur.
 *
 *   adım 0  B+1,5 sa  (maç bitişi + 30 dk) "Maç bitti: Siyah 5–4 Beyaz · oylama açıldı" → maçta oynayan herkes
 *   adım 1  B+3 sa    (maç bitişi + 2 sa)  → oy vermeyenler
 *   adım 2  B+12 sa                         → oy vermeyenler
 *   adım 3–6 B+16 / 19 / 21 / 22 sa (aralık 4, 3, 2, 1 sa daralır; 21:00 maçında ertesi gün 19:00'da biter)
 *
 * Hatırlatmalar (adım ≥1) 00:00–08:00 arasına düşerse atlanır (ertelenmez). Sunucu kapalı kalıp bir adım
 * 1 saatten fazla gecikirse o adım da atlanır. Maç başına gönderilen son adım game_notification_state'te.
 */
@Component
public class VoteReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(VoteReminderScheduler.class);

    private static final int[] STEP_MINUTES = {90, 180, 720, 960, 1140, 1260, 1320};
    private static final int QUIET_UNTIL_HOUR = 8;
    private static final Duration MAX_DELAY = Duration.ofHours(1);
    private static final Locale TR = Locale.forLanguageTag("tr");

    private final GameRepository gameRepository;
    private final RosterRepository rosterRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final GameNotificationStateRepository stateRepository;
    private final NotificationService notificationService;
    private final TransactionTemplate tx;

    public VoteReminderScheduler(GameRepository gameRepository, RosterRepository rosterRepository,
                                 GroupMembershipRepository groupMembershipRepository,
                                 GameNotificationStateRepository stateRepository,
                                 NotificationService notificationService,
                                 PlatformTransactionManager transactionManager) {
        this.gameRepository = gameRepository;
        this.rosterRepository = rosterRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.stateRepository = stateRepository;
        this.notificationService = notificationService;
        this.tx = new TransactionTemplate(transactionManager);
    }

    /** Gönderilecek bildirim: durum işlendikten (commit) sonra, işlem dışında gönderilir. */
    private record Push(Set<Integer> userIds, String title, String body, Map<String, Object> data) {
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        int lastStep = STEP_MINUTES[STEP_MINUTES.length - 1];
        List<Integer> gameIds = gameRepository
                .findUnvotedStartedBetween(now.minusMinutes(lastStep + 60), now.minusMinutes(STEP_MINUTES[0]))
                .stream().map(Game::getId).toList();
        for (Integer gameId : gameIds) {
            try {
                Push push = tx.execute(s -> process(gameId, now));
                if (push != null && !push.userIds().isEmpty()) {
                    notificationService.sendPushToUsers(push.userIds(), push.title(), push.body(), push.data());
                }
            } catch (Exception e) {
                log.error("Vote reminder failed: gameId={}", gameId, e);
            }
        }
    }

    private Push process(Integer gameId, LocalDateTime now) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null || game.isVoted() || game.getSquad() == null) return null;
        LocalDateTime kickoff = game.getDateTime();

        int step = -1;
        for (int i = 0; i < STEP_MINUTES.length; i++) {
            if (!kickoff.plusMinutes(STEP_MINUTES[i]).isAfter(now)) step = i;
        }
        GameNotificationState state = stateRepository.findById(gameId).orElseGet(() -> new GameNotificationState(gameId));
        if (step <= state.getLastStep()) return null;
        state.setLastStep(step);
        stateRepository.save(state);

        LocalDateTime due = kickoff.plusMinutes(STEP_MINUTES[step]);
        if (step > 0 && (isQuiet(due) || isQuiet(now) || now.isAfter(due.plus(MAX_DELAY)))) return null;

        Integer squadId = game.getSquad().getId();
        Map<Integer, Integer> userByPlayer = groupMembershipRepository.findBySquadId(squadId).stream()
                .filter(GroupMembership::isActive)
                .collect(Collectors.toMap(gm -> gm.getPlayer().getId(), gm -> gm.getUser().getId(), (a, b) -> a));
        List<Roster> rosters = rosterRepository.findAllByGameId(gameId).stream()
                .filter(r -> userByPlayer.containsKey(r.getPlayer().getId()))
                .toList();
        List<Roster> recipients = step == 0 ? rosters
                : rosters.stream().filter(r -> !Boolean.TRUE.equals(r.getHasVote())).toList();
        Set<Integer> userIds = recipients.stream().map(r -> userByPlayer.get(r.getPlayer().getId())).collect(Collectors.toSet());

        Map<String, Object> data = new HashMap<>();
        data.put("gameId", gameId);
        data.put("squadId", squadId);
        if (step == 0) {
            data.put("type", "GAME_ENDED");
            String title = "Maç bitti: Siyah " + score(game.getHomeTeamScore()) + "–" + score(game.getAwayTeamScore()) + " Beyaz";
            return new Push(userIds, title, "Oylama açıldı, takım arkadaşlarını puanla.", data);
        }
        data.put("type", "VOTE_REMINDER");
        String day = kickoff.getDayOfWeek().getDisplayName(TextStyle.FULL, TR);
        String[] text = reminderText(step, day, recipients.size());
        return new Push(userIds, text[0], text[1], data);
    }

    private static String[] reminderText(int step, String day, int nonVoters) {
        return switch (step) {
            case 1 -> new String[]{"Oyunu bekliyoruz", "Takım arkadaşların puanını bekliyor."};
            case 2 -> new String[]{"Oylama sürüyor", day + " maçının oylamasında senin oyun eksik."};
            case 3 -> new String[]{"Hâlâ oy vermedin", "Takım arkadaşlarını puanla, 1 dakikanı alır."};
            case 4 -> new String[]{"MVP seni bekliyor", "Senin oyun olmadan MVP belli olmuyor."};
            case 5 -> nonVoters <= 1
                    ? new String[]{"Bir tek sen kaldın", "Senin oyunla oylama tamamlanıyor."}
                    : new String[]{"Oy vermeyen " + nonVoters + " kişi kaldı", "Biri sensin. Oylama bu akşam bitsin."};
            default -> new String[]{"Son hatırlatma", day + " maçı için oyun hâlâ eksik."};
        };
    }

    private static boolean isQuiet(LocalDateTime t) {
        return t.getHour() < QUIET_UNTIL_HOUR;
    }

    private static int score(Integer s) {
        return s == null ? 0 : s;
    }
}
