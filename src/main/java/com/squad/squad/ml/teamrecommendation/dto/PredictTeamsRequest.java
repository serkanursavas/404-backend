package com.squad.squad.ml.teamrecommendation.dto;

import java.util.List;

public class PredictTeamsRequest {
    private List<Long> playerIds;

    public PredictTeamsRequest() {}

    public PredictTeamsRequest(List<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public List<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<Long> playerIds) {
        this.playerIds = playerIds;
    }
}