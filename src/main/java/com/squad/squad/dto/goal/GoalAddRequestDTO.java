package com.squad.squad.dto.goal;

public class GoalAddRequestDTO {
    private Integer gameId;
    private Integer playerId;
    private String teamColor;

    public GoalAddRequestDTO(Integer gameId, Integer playerId, String teamColor) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.teamColor = teamColor;
    }

    public GoalAddRequestDTO() {
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(String teamColor) {
        this.teamColor = teamColor;
    }
}