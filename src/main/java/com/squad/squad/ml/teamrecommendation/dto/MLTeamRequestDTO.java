package com.squad.squad.ml.teamrecommendation.dto;


import java.util.List;

public class MLTeamRequestDTO {

    private List<MLPlayerDTO> players;

    // Boş kurucu
    public MLTeamRequestDTO() {}

    // Tek alanlı kurucu
    public MLTeamRequestDTO(List<MLPlayerDTO> players) {
        this.players = players;
    }

    // Getter ve Setter
    public List<MLPlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<MLPlayerDTO> players) {
        this.players = players;
    }
}