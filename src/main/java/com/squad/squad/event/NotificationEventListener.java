package com.squad.squad.event;

import com.squad.squad.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGoalScored(GoalScoredEvent event) {
        try {
            notificationService.sendPushToSquad(
                    event.getSquadId(),
                    "⚽ Gol!",
                    "Maçın skoru belli oldu!",
                    Map.of("gameId", event.getGameId(), "squadId", event.getSquadId(), "type", "GOAL_SCORED"),
                    event.getActorUserId()
            );
        } catch (Exception e) {
            log.error("Failed to send push notification for GoalScoredEvent: gameId={}", event.getGameId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameCreated(GameCreatedEvent event) {
        try {
            String dateStr = event.getGameDateTime() != null
                    ? event.getGameDateTime().format(FORMATTER)
                    : "";
            String body = event.getLocationName() + " • " + dateStr;
            notificationService.sendPushToSquad(
                    event.getSquadId(),
                    "📅 Yeni maç oluşturuldu!",
                    body,
                    Map.of("gameId", event.getGameId(), "squadId", event.getSquadId(), "type", "GAME_CREATED"),
                    event.getActorUserId()
            );
        } catch (Exception e) {
            log.error("Failed to send push notification for GameCreatedEvent: gameId={}", event.getGameId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameUpdated(GameUpdatedEvent event) {
        try {
            String dateStr = event.getGameDateTime() != null
                    ? event.getGameDateTime().format(FORMATTER)
                    : "";
            String body = event.getLocationName() + " • " + dateStr;
            notificationService.sendPushToSquad(
                    event.getSquadId(),
                    "🔄 Maç güncellendi",
                    body,
                    Map.of("gameId", event.getGameId(), "squadId", event.getSquadId(), "type", "GAME_UPDATED"),
                    event.getActorUserId()
            );
        } catch (Exception e) {
            log.error("Failed to send push notification for GameUpdatedEvent: gameId={}", event.getGameId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMVPAnnounced(MVPAnnouncedEvent event) {
        try {
            notificationService.sendPushToSquad(
                    event.getSquadId(),
                    "🏆 Oylamalar tamamlandı!",
                    "Puanları ve MVP'yi görüntüle!",
                    Map.of("gameId", event.getGameId(), "squadId", event.getSquadId(), "type", "MVP_ANNOUNCED")
            );
        } catch (Exception e) {
            log.error("Failed to send push notification for MVPAnnouncedEvent: gameId={}", event.getGameId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJoinRequestApproved(JoinRequestApprovedEvent event) {
        try {
            notificationService.sendPushToUser(
                    event.getUserId(),
                    "✅ Hoş geldin!",
                    event.getSquadName() + "'ın bir parçasısın!",
                    Map.of("squadId", event.getSquadId(), "type", "JOIN_REQUEST_APPROVED")
            );
        } catch (Exception e) {
            log.error("Failed to send push notification for JoinRequestApprovedEvent: userId={}", event.getUserId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJoinRequestRejected(JoinRequestRejectedEvent event) {
        try {
            notificationService.sendPushToUser(
                    event.getUserId(),
                    "❌ Katılım reddedildi",
                    event.getSquadName() + " üyelik talebiniz reddedildi.",
                    Map.of("type", "JOIN_REQUEST_REJECTED")
            );
        } catch (Exception e) {
            log.error("Failed to send push notification for JoinRequestRejectedEvent: userId={}", event.getUserId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSquadApproved(SquadApprovedEvent event) {
        try {
            notificationService.sendPushToUser(
                    event.getUserId(),
                    "✅ Squad onaylandı!",
                    event.getSquadName() + " onaylandı! Davet kodu hazır.",
                    Map.of("type", "SQUAD_APPROVED")
            );
        } catch (Exception e) {
            log.error("Failed to send push notification for SquadApprovedEvent: userId={}", event.getUserId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberRemoved(MemberRemovedEvent event) {
        try {
            notificationService.sendPushToUser(
                    event.getUserId(),
                    "❌ Squad'dan çıkarıldın",
                    event.getSquadName() + " kadrosundan çıkarıldın.",
                    Map.of("type", "MEMBER_REMOVED")
            );
        } catch (Exception e) {
            log.error("Failed to send push notification for MemberRemovedEvent: userId={}", event.getUserId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJoinRequestReceived(JoinRequestReceivedEvent event) {
        try {
            notificationService.sendPushToSquadAdmins(
                    event.getSquadId(),
                    "📩 Yeni üyelik talebi",
                    event.getRequesterName() + " squad'a katılmak istiyor.",
                    Map.of("squadId", event.getSquadId(), "type", "JOIN_REQUEST_RECEIVED")
            );
        } catch (Exception e) {
            log.error("Failed to send push notification for JoinRequestReceivedEvent: squadId={}", event.getSquadId(), e);
        }
    }
}
