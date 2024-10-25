package com.squad.squad.dto.game;

import com.squad.squad.dto.GoalDTO;
import com.squad.squad.dto.RosterDTO;
import com.squad.squad.dto.roster.RosterCreateDTO;
import com.squad.squad.dto.roster.RosterUpdateDTO;

import java.time.LocalDateTime;
import java.util.List;

public class GameUpdateRequestDTO {
    private Integer id;
    private String location;
    private LocalDateTime dateTime;
    private Integer teamSize;
    private List<RosterUpdateDTO> rosters;

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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<RosterUpdateDTO> getRosters() {
        return rosters;
    }

    public void setRosters(List<RosterUpdateDTO> rosters) {
        this.rosters = rosters;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }
}