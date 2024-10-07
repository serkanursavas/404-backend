package com.squad.squad.dto;

public class GoalDTO {
    private Integer gameId;
    private Integer playerId;
    private String playerName;
    private String teamColor;

    public GoalDTO(Integer gameId, Integer playerId, String playerName, String teamColor) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamColor = teamColor;
    }

    public GoalDTO() {
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

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(String teamColor) {
        this.teamColor = teamColor;
    }

}
