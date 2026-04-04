package com.squad.squad.event;

public class GoalScoredEvent {

    private final Integer gameId;
    private final Integer squadId;
    private final int goalCount;
    private final Integer actorUserId;

    public GoalScoredEvent(Integer gameId, Integer squadId, int goalCount, Integer actorUserId) {
        this.gameId = gameId;
        this.squadId = squadId;
        this.goalCount = goalCount;
        this.actorUserId = actorUserId;
    }

    public Integer getGameId() {
        return gameId;
    }

    public Integer getSquadId() {
        return squadId;
    }

    public int getGoalCount() {
        return goalCount;
    }

    public Integer getActorUserId() {
        return actorUserId;
    }
}
