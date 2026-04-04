package com.squad.squad.event;

public class MemberRemovedEvent {
    private final Integer userId;
    private final String squadName;

    public MemberRemovedEvent(Integer userId, String squadName) {
        this.userId = userId;
        this.squadName = squadName;
    }

    public Integer getUserId() { return userId; }
    public String getSquadName() { return squadName; }
}
