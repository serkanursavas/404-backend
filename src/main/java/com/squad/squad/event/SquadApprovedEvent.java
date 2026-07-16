package com.squad.squad.event;

public class SquadApprovedEvent {
    private final Integer userId;
    private final Integer squadId;
    private final String squadName;

    public SquadApprovedEvent(Integer userId, Integer squadId, String squadName) {
        this.userId = userId;
        this.squadId = squadId;
        this.squadName = squadName;
    }

    public Integer getUserId() { return userId; }
    public Integer getSquadId() { return squadId; }
    public String getSquadName() { return squadName; }
}
