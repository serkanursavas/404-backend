package com.squad.squad.dto.game;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.dto.roster.RosterCreateDTO;

import java.time.LocalDateTime;
import java.util.List;

public class GameCreateRequestDTO {
    private String location;
    private String weather;
    private LocalDateTime dateTime;
    private Integer teamSize;
    private List<RosterCreateDTO> rosters;

    // Getters and Setters
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

    public List<RosterCreateDTO> getRosters() {
        return rosters;
    }

    public void setRosters(List<RosterCreateDTO> rosters) {
        this.rosters = rosters;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }
}