package com.squad.squad.ml.teamrecommendation.dto;

import java.util.List;

public class MLPlayerDTO {
    private Long id;
    private String name;
    private double rating;
    private String position;
    private int goalsTotal;
    private List<String> personas;
    private double recentForm;

    // Boş kurucu
    public MLPlayerDTO() {}

    // Tüm alanları içeren kurucu (opsiyonel)
    public MLPlayerDTO(Long id, String name, double rating,
                     String position, int goalsTotal,
                     List<String> personas, double recentForm) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.position = position;
        this.goalsTotal = goalsTotal;
        this.personas = personas;
        this.recentForm = recentForm;
    }

    // Getter ve Setter metodları
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public int getGoalsTotal() { return goalsTotal; }
    public void setGoalsTotal(int goalsTotal) { this.goalsTotal = goalsTotal; }

    public List<String> getPersonas() { return personas; }
    public void setPersonas(List<String> personas) { this.personas = personas; }

    public double getRecentForm() { return recentForm; }
    public void setRecentForm(double recentForm) { this.recentForm = recentForm; }
}