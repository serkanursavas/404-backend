package com.squad.squad.dto.roster;

import com.squad.squad.dto.RosterDTO;

public class RosterCreateDTO extends RosterDTO {

    private String teamColor;
    private Integer playerId;

    // Default constructor
    public RosterCreateDTO() {
    }

    // Getters and Setters
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