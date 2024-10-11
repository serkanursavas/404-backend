package com.squad.squad.dto.goal;

public class GoalResponseDTO {

    private Integer playerId;
    private String playerName;
    private String teamColor;

    public GoalResponseDTO(Integer playerId, String playerName, String teamColor) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamColor = teamColor;
    }

    public GoalResponseDTO() {
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