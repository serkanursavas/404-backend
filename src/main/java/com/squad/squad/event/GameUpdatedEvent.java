package com.squad.squad.event;

import java.time.LocalDateTime;

public class GameUpdatedEvent {

    private final Integer gameId;
    private final Integer squadId;
    private final LocalDateTime gameDateTime;
    private final String locationName;
    private final Integer actorUserId;

    public GameUpdatedEvent(Integer gameId, Integer squadId, LocalDateTime gameDateTime, String locationName, Integer actorUserId) {
        this.gameId = gameId;
        this.squadId = squadId;
        this.gameDateTime = gameDateTime;
        this.locationName = locationName;
        this.actorUserId = actorUserId;
    }

    public Integer getGameId() { return gameId; }
    public Integer getSquadId() { return squadId; }
    public LocalDateTime getGameDateTime() { return gameDateTime; }
    public String getLocationName() { return locationName; }
    public Integer getActorUserId() { return actorUserId; }
}
