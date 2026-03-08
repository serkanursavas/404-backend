package com.squad.squad.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
public class Rating extends BaseEntity {

    @Column(nullable = false)
    private Integer rate;

    @ManyToOne
    @JoinColumn(name = "roster_id", referencedColumnName = "id")
    @JsonManagedReference
    private Roster roster;

    @ManyToOne
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    @JsonManagedReference
    private Player player;

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Roster getRoster() {
        return roster;
    }

    public void setRoster(Roster roster) {
        this.roster = roster;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
