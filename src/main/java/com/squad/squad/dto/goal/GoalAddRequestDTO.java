package com.squad.squad.dto.goal;

public class GoalAddRequestDTO {
    private Integer playerId;
    private String teamColor;

    public GoalAddRequestDTO(Integer playerId, String teamColor) {
        this.playerId = playerId;
        this.teamColor = teamColor;
    }

    public GoalAddRequestDTO() {
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