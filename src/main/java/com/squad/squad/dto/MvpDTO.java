package com.squad.squad.dto;

public class MvpDTO {
    private Integer id;
    private String name;
    private String surname;
    private String photo;
    private String position;
    private double rating; // double olarak tanımlanmalı

    public MvpDTO(Integer id, String name, String surname, String photo, String position, double rating) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.photo = photo;
        this.position = position;
        this.rating = rating;
    }

    // Getter ve Setter metodları
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

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}