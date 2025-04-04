package com.squad.squad.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

public class PlayerDTO {

    private Integer id;
    private String name;
    private String surname;
    private String foot;
    private String photo;
    private double rating;
    private String position;
    private boolean active = true;
    private List<PlayerPersonaDTO> personas;
    private List<Double> last5GameRating;

    public double getRating() {
        return Math.round(rating * 100.0) / 100.0;
    }

    public void setRating(double rating) {
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
        return last5GameRating;
    }

    public void setLast5GameRating(List<Double> last5GameRating) {
        this.last5GameRating = last5GameRating;
    }

}