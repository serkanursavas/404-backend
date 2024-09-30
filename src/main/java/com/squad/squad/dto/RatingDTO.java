package com.squad.squad.dto;

public class RatingDTO {

    private Integer player_id;
    private Integer rate;
    private Integer roster_id;
    private Integer id;

    // Constructor
    public RatingDTO(Integer player_id, Integer rate, Integer roster_id, Integer id) {
        this.player_id = player_id;
        this.rate = rate;
        this.roster_id = roster_id;
        this.id = id;
    }

    // Getters and Setters
    public Integer getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(Integer player_id) {
        this.player_id = player_id;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Integer getRoster_id() {
        return roster_id;
    }

    public void setRoster_id(Integer roster_id) {
        this.roster_id = roster_id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}