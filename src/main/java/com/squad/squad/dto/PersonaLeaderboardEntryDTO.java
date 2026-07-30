package com.squad.squad.dto;

public class PersonaLeaderboardEntryDTO {
    private Integer playerId;
    private String name;
    private String surname;
    private String position;
    private Integer count;

    public PersonaLeaderboardEntryDTO(Integer playerId, String name, String surname, String position, Integer count) {
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.position = position;
        this.count = count;
    }

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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
