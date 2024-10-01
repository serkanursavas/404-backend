package com.squad.squad.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GameDTO {
    private Integer id;
    private String location;
    private String weather;
    private Integer homeTeamScore;
    private Integer awayTeamScore;
    private LocalDateTime dateTime;
    private List<RosterDTO> rosters; // 12 kişilik roster listesi
    private List<GoalDTO> goals;

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<RosterDTO> getRosters() {
        return rosters;
    }

    public void setRosters(List<RosterDTO> rosters) {
        this.rosters = rosters;
    }

    public List<GoalDTO> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalDTO> goals) {
        this.goals = goals;
    }
}