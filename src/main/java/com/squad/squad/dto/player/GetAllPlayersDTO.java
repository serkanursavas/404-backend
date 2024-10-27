package com.squad.squad.dto.player;

import com.squad.squad.entity.Goal;
import com.squad.squad.entity.Roster;
import com.squad.squad.entity.User;
import jakarta.persistence.*;

import java.util.List;

public class GetAllPlayersDTO {

    private Integer id;
    private String name;
    private String surname;
    private String foot;
    private String photo;
    private double rating;
    private String position;
    private boolean active = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFoot() {
        return foot;
    }

    public void setFoot(String foot) {
        this.foot = foot;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public double getRating() {
        return Math.round(rating * 100.0) / 100.0;
    }

    public void setRating(double rating) {
        this.rating = rating;
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
}