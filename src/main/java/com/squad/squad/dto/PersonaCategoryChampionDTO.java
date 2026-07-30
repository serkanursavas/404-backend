package com.squad.squad.dto;

public class PersonaCategoryChampionDTO {
    private String category;
    private Integer playerId;
    private String name;
    private String surname;
    private String position;
    private Integer total;

    public PersonaCategoryChampionDTO(String category, Integer playerId, String name, String surname, String position, Integer total) {
        this.category = category;
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.position = position;
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
