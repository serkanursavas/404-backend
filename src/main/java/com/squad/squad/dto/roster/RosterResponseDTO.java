package com.squad.squad.dto.roster;

import jakarta.validation.constraints.NotNull;

public class RosterResponseDTO {

    @NotNull(message = "Player ID cannot be null")
    private Integer id;
    private String teamColor;
    private double rating;
    private Integer playerId;
    private String playerName;

    // Default constructor
    public RosterResponseDTO() {
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
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
}