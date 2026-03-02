package com.squad.squad.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.squad.squad.dto.squad.SquadSummaryDTO;

import java.util.List;

public class AuthResponseDTO {

    private String token;
    private List<SquadSummaryDTO> squads;
    @JsonProperty("isSuperAdmin")
    private boolean isSuperAdmin;
    private int pendingRequestCount;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<SquadSummaryDTO> getSquads() {
        return squads;
    }

    public void setSquads(List<SquadSummaryDTO> squads) {
        this.squads = squads;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }

    public int getPendingRequestCount() {
        return pendingRequestCount;
    }

    public void setPendingRequestCount(int pendingRequestCount) {
        this.pendingRequestCount = pendingRequestCount;
    }
}
