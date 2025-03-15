package com.squad.squad.dto;

public class TopListsDTO {
    private Integer playerId;
    private String name;
    private String surname;
    private Long goalCount;  // Goal sayısı isteğe bağlı, bu yüzden Long
    private Long gameCount;  // Game sayısı isteğe bağlı, bu yüzden Long
    private Double rating;   // Rating isteğe bağlı, bu yüzden Double

    // Constructor for top scorer list (goalCount)
    public TopListsDTO(Integer playerId, String name, String surname, Long goalCount, Long gameCount) {
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.goalCount = goalCount;
        this.gameCount = gameCount; // Game count değeri mevcut değilse null bırak
        this.rating = null; // Rating değeri mevcut değilse null bırak
    }

    // Constructor for top rated list (rating)
    public TopListsDTO(Integer playerId, String name, String surname, Double rating,Long gameCount) {
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.goalCount = null; // Goal count değeri mevcut değilse null bırak
        this.gameCount = gameCount; // Game count değeri mevcut değilse null bırak
        this.rating = rating;
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

    public Double getRating() {
        // Rating null ise, null döner, değilse yuvarlanmış değeri döner
        return rating != null ? Math.round(rating * 100.0) / 100.0 : null;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getGameCount() {
        return gameCount;
    }

    public void setGameCount(Long gameCount) {
        this.gameCount = gameCount;
    }
}