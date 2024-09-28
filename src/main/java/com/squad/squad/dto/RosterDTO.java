package com.squad.squad.dto;

public class RosterDTO {

    private String teamColor;
    private double rating;
    private Integer playerId; // Player entity'den sadece ID taşınıyor
    private String playerName;

    // Default constructor
    public RosterDTO() {
    }

    // Getters and Setters

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
