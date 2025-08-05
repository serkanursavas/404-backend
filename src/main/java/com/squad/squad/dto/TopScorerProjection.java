package com.squad.squad.dto;

public interface TopScorerProjection {
    Integer getPlayerId();

    String getName();

    String getSurname();

    Long getGoalCount();

    Long getRosterCount();
}