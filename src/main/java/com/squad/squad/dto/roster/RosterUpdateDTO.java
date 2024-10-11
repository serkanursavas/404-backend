package com.squad.squad.dto.roster;

import com.squad.squad.dto.RosterDTO;
import jakarta.validation.constraints.NotNull;

public class RosterUpdateDTO extends RosterDTO {

    @NotNull(message = "Player ID cannot be null")
    private Integer id;
    private String teamColor;
    private Integer playerId;
    private Integer gameId;

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

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }
}