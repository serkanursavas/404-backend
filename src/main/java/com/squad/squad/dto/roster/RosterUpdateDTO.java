package com.squad.squad.dto.roster;

import jakarta.validation.constraints.NotNull;

public class RosterUpdateDTO {

    @NotNull(message = "Player ID cannot be null")
    private Integer id;
    private String teamColor;
    private Integer playerId;

    // Default constructor
    public RosterUpdateDTO() {
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(String teamColor) {
        this.teamColor = teamColor;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }
}