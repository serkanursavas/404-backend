package com.squad.squad.event;

/** Maç bittikten sonra (canlı pencere dışında) gol girişi/düzeltmesi skoru değiştirdi. */
public class ScoreUpdatedEvent {

    private final Integer gameId;
    private final Integer squadId;
    private final int homeTeamScore;
    private final int awayTeamScore;
    private final Integer actorUserId;

    public ScoreUpdatedEvent(Integer gameId, Integer squadId, int homeTeamScore, int awayTeamScore, Integer actorUserId) {
        this.gameId = gameId;
        this.squadId = squadId;
        this.homeTeamScore = homeTeamScore;
        this.awayTeamScore = awayTeamScore;
        this.actorUserId = actorUserId;
    }

    public Integer getGameId() {
        return gameId;
    }

    public Integer getSquadId() {
        return squadId;
    }

    public int getHomeTeamScore() {
        return homeTeamScore;
    }

    public int getAwayTeamScore() {
        return awayTeamScore;
    }

    public Integer getActorUserId() {
        return actorUserId;
    }
}
