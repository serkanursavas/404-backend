package com.squad.squad.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private boolean isPlayed = false;

    @Column(nullable = false)
    private boolean isVoted = false;

    @Column(nullable = false)
    private String location;

    @Column(nullable = true)
    private Integer mvpId;

    @ManyToOne
    private GameLocation gameLocation;

    @Column(nullable = false)
    private String weather;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime dateTime;

    private Integer homeTeamScore = 0;

    private Integer awayTeamScore = 0;

    @OneToMany(mappedBy = "game")
    @JsonBackReference
    private List<Roster> roster;

    @OneToMany(mappedBy = "game")
    @JsonIgnore
    private List<Goal> goal;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isPlayed() {
        return isPlayed;
    }

    public void setPlayed(boolean isPlayed) {
        this.isPlayed = isPlayed;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getHomeTeamScore() {
        return homeTeamScore;
    }

    public void setHomeTeamScore(Integer homeTeamScore) {
        this.homeTeamScore = homeTeamScore;
    }

    public Integer getAwayTeamScore() {
        return awayTeamScore;
    }

    public void setAwayTeamScore(Integer awayTeamScore) {
        this.awayTeamScore = awayTeamScore;
    }

    public List<Roster> getRoster() {
        return roster;
    }

    public void setRoster(List<Roster> roster) {
        this.roster = roster;
    }

    public List<Goal> getGoal() {
        return goal;
    }

    public void setGoal(List<Goal> goal) {
        this.goal = goal;
    }

    public boolean isVoted() {
        return isVoted;
    }

    public void setVoted(boolean voted) {
        isVoted = voted;
    }

    // missed getter and setters
    public GameLocation getGameLocation() {
        return gameLocation;
    }

    public void setGameLocation(GameLocation gameLocation) {
        this.gameLocation = gameLocation;
    }

    public Integer getMvpId() {
        return mvpId;
    }

    public void setMvpId(Integer mvpId) {
        this.mvpId = mvpId;
    }


}