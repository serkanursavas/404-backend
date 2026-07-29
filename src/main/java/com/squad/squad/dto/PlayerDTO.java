package com.squad.squad.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

public class PlayerDTO {

    private Integer id;
    private String name;
    private String surname;
    private String foot;
    private String photo;
    private Double rating;
    private String position;
    private boolean active = true;
    private List<PlayerPersonaDTO> personas;
    private List<Double> last5GameRating;
    private Integer totalGoals;
    private Integer gamesPlayed;
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer mvpCount;

    public Double getRating() {
        return rating != null ? Math.round(rating * 10.0) / 10.0 : null;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getFoot() {
        return foot;
    }

    public void setFoot(String foot) {
        this.foot = foot;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<PlayerPersonaDTO> getPersonas() {
        return personas;
    }

    public void setPersonas(List<PlayerPersonaDTO> personas) {
        this.personas = personas;
    }

    public List<Double> getLast5GameRating() {
        if (last5GameRating == null) {
            return null;
        }
        return last5GameRating.stream()
                .map(rating -> rating != null ? Math.round(rating * 10.0) / 10.0 : null)
                .collect(java.util.stream.Collectors.toList());
    }

    public void setLast5GameRating(List<Double> last5GameRating) {
        this.last5GameRating = last5GameRating;
    }

    public Integer getTotalGoals() {
        return totalGoals;
    }

    public void setTotalGoals(Integer totalGoals) {
        this.totalGoals = totalGoals;
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getDraws() {
        return draws;
    }

    public void setDraws(Integer draws) {
        this.draws = draws;
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public Integer getMvpCount() {
        return mvpCount;
    }

    public void setMvpCount(Integer mvpCount) {
        this.mvpCount = mvpCount;
    }

}