package com.squad.squad.event;

public class SquadApprovedEvent {
    private final Integer userId;
    private final String squadName;

    public SquadApprovedEvent(Integer userId, String squadName) {
        this.userId = userId;
        this.squadName = squadName;
    }

    public Integer getUserId() { return userId; }
    public String getSquadName() { return squadName; }
}
