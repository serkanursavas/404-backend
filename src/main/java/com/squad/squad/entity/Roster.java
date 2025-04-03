package com.squad.squad.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Roster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String teamColor;

    @Column(nullable = true)
    private double rating;

    @Column(nullable = true)
    private Integer persona1;

    @Column(nullable = true)
    private Integer persona2;

    @Column(nullable = true)
    private Integer persona3;

    @Column(name = "has_vote", nullable = true)
    private Boolean hasVote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "id", nullable = false)
    private Player player;

    @OneToMany(mappedBy = "roster")
    @JsonIgnore
    private List<Rating> rate;

    public Roster() {
    }

    public Roster(Integer id, String teamColor, Game game, Player player) {
        this.id = id;
        this.teamColor = teamColor;
        this.game = game;
        this.player = player;
    }

    public Boolean getHasVote() {
        return hasVote;
    }

    public void setHasVote(Boolean hasVote) {
        this.hasVote = hasVote;
    }

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

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Rating> getRate() {
        return rate;
    }

    public void setRate(List<Rating> rate) {
        this.rate = rate;
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