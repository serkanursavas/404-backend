package com.squad.squad.dto;

public class MvpDTO {
    private Integer id;
    private String name;
    private String surname;
    private String photo;
    private String position;
    private double rating; // double olarak tanımlanmalı
    private Integer persona1Id;
    private Integer persona2Id;
    private Integer persona3Id;

    public MvpDTO(Integer id, String name, String surname, String photo, String position, double rating,
            Integer persona1Id, Integer persona2Id, Integer persona3Id) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.photo = photo;
        this.position = position;
        this.rating = rating;
        this.persona1Id = persona1Id;
        this.persona2Id = persona2Id;
        this.persona3Id = persona3Id;
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

    public double getRating() {
        return Math.round(rating * 10.0) / 10.0;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getPersona1Id() {
        return persona1Id;
    }

    public void setPersona1Id(Integer persona1Id) {
        this.persona1Id = persona1Id;
    }

    public Integer getPersona2Id() {
        return persona2Id;
    }

    public void setPersona2Id(Integer persona2Id) {
        this.persona2Id = persona2Id;
    }

    public Integer getPersona3Id() {
        return persona3Id;
    }

    public void setPersona3Id(Integer persona3Id) {
        this.persona3Id = persona3Id;
    }
}