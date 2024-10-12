package com.squad.squad.dto.goal;

import java.util.List;

public class AddGoalsRequestDTO {
    private Integer gameId;
    private List<GoalAddRequestDTO> goals;

    // Constructor, Getters ve Setters
    public AddGoalsRequestDTO(Integer gameId, List<GoalAddRequestDTO> goals) {
        this.gameId = gameId;
        this.goals = goals;
    }

    public AddGoalsRequestDTO() {
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public List<GoalAddRequestDTO> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalAddRequestDTO> goals) {
        this.goals = goals;
    }
}