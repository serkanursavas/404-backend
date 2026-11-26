package com.squad.squad.entity;

import jakarta.persistence.*;

/** Maç sonrası bildirim takviminde maç başına gönderilen son adım (bkz. VoteReminderScheduler). */
@Entity
@Table(name = "game_notification_state")
public class GameNotificationState {

    @Id
    @Column(name = "game_id")
    private Integer gameId;

    @Column(name = "last_step", nullable = false)
    private int lastStep = -1;

    protected GameNotificationState() {
    }

    public GameNotificationState(Integer gameId) {
        this.gameId = gameId;
    }

    public Integer getGameId() {
        return gameId;
    }

    public int getLastStep() {
        return lastStep;
    }

    public void setLastStep(int lastStep) {
        this.lastStep = lastStep;
    }
}
