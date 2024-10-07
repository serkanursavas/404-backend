package com.squad.squad.dto;

public class RatingDTO {

    private Integer playerId;
    private Integer rate;
    private Integer rosterId;
    private Integer id;

    // Constructor
    public RatingDTO(Integer playerId, Integer rate, Integer rosterId, Integer id) {
        this.playerId = playerId;
        this.rate = rate;
        this.rosterId = rosterId;
        this.id = id;
    }

    // Getters and Setters
    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Integer getRosterId() {
        return rosterId;
    }

    public void setRosterId(Integer rosterId) {
        this.rosterId = rosterId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}