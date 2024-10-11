package com.squad.squad.dto.game;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.dto.roster.RosterResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public class GameResponseDTO {
    private Integer id;
    private String location;
    private String weather;
    private Integer homeTeamScore;
    private Integer awayTeamScore;
    private LocalDateTime dateTime;
    private List<RosterResponseDTO> rosters;
    private List<GoalDTO> goals;
    private boolean isPlayed;
    private boolean voteMode;

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

    public List<RosterResponseDTO> getRosters() {
        return rosters;
    }

    public void setRosters(List<RosterResponseDTO> rosters) {
        this.rosters = rosters;
    }

    public List<GoalDTO> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalDTO> goals) {
        this.goals = goals;
    }

    public boolean isPlayed() {
        return isPlayed;
    }

    public void setPlayed(boolean isPlayed) {
        this.isPlayed = isPlayed;
    }

    public boolean isVoteMode() {
        return voteMode;
    }

    public void setVoteMode(boolean voteMode) {
        this.voteMode = voteMode;
    }
}