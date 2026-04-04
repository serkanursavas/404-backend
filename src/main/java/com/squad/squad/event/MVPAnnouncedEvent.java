package com.squad.squad.event;

public class MVPAnnouncedEvent {
    private final Integer gameId;
    private final Integer squadId;
    private final String mvpPlayerName;

    public MVPAnnouncedEvent(Integer gameId, Integer squadId, String mvpPlayerName) {
        this.gameId = gameId;
        this.squadId = squadId;
        this.mvpPlayerName = mvpPlayerName;
    }

    public Integer getGameId() { return gameId; }
    public Integer getSquadId() { return squadId; }
    public String getMvpPlayerName() { return mvpPlayerName; }
}
