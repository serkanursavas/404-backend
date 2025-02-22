package com.squad.squad.dto.roster;

import jakarta.validation.constraints.NotNull;

public class RosterResponseDTO {

    @NotNull(message = "Player ID cannot be null")
    private Integer id;
    private String teamColor;
    private double rating;
    private Integer playerId;
    private String playerName;
    private Integer persona1;
    private Integer persona2;
    private Integer persona3;

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
        return Math.round(rating * 100.0) / 100.0;
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

    public Integer getPersona1() {
        return persona1;
    }

    public void setPersona1(Integer persona1) {
        this.persona1 = persona1;
    }

    public Integer getPersona2() {
        return persona2;
    }

    public void setPersona2(Integer persona2) {
        this.persona2 = persona2;
    }

    public Integer getPersona3() {
        return persona3;
    }

    public void setPersona3(Integer persona3) {
        this.persona3 = persona3;
    }
}