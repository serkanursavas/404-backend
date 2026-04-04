package com.squad.squad.event;

public class JoinRequestReceivedEvent {

    private final Integer squadId;
    private final String requesterName;

    public JoinRequestReceivedEvent(Integer squadId, String requesterName) {
        this.squadId = squadId;
        this.requesterName = requesterName;
    }

    public Integer getSquadId() { return squadId; }
    public String getRequesterName() { return requesterName; }
}
