package com.squad.squad.dto;

public interface TopListProjection {
    Integer getPlayerId();
    String getName();
    String getSurname();
    Double getFormScore(); // Form skoru (Ağırlıklı Ortalama + Mutlak Değişim)
    Double getRating();
    Double getAvgRatingChange();

    Integer getPlayer1Id();
    String getPlayer1Name();
    Integer getPlayer2Id();
    String getPlayer2Name();
    Integer getGamesTogether();
    Integer getGamesAgainst();
}