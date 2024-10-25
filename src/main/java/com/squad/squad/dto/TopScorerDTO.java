package com.squad.squad.dto;

public class TopScorerDTO {
    private Integer playerId;
    private String name;
    private String surname;
    private Long goalCount;

    public TopScorerDTO(Integer playerId, String name, String surname, Long goalCount) {
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.goalCount = goalCount;
    }

    // Getter ve Setter metodları
    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Long getGoalCount() {
        return goalCount;
    }

    public void setGoalCount(Long goalCount) {
        this.goalCount = goalCount;
    }
}