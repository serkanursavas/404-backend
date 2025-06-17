package com.squad.squad.ml.teamrecommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MLTeamResponseDTO {

    @JsonProperty("team_blue")
    private List<MLPlayerDTO> teamBlue;

    @JsonProperty("team_red")
    private List<MLPlayerDTO> teamRed;

    public MLTeamResponseDTO() {}

    public MLTeamResponseDTO(List<MLPlayerDTO> teamBlue, List<MLPlayerDTO> teamRed) {
        this.teamBlue = teamBlue;
        this.teamRed = teamRed;
    }

    public List<MLPlayerDTO> getTeamBlue() {
        return teamBlue;
    }

    public void setTeamBlue(List<MLPlayerDTO> teamBlue) {
        this.teamBlue = teamBlue;
    }

    public List<MLPlayerDTO> getTeamRed() {
        return teamRed;
    }

    public void setTeamRed(List<MLPlayerDTO> teamRed) {
        this.teamRed = teamRed;
    }
}