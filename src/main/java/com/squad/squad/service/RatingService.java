package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.rating.AddRatingRequestDTO;
import com.squad.squad.entity.Roster;

public interface RatingService {

    void saveRating(List<AddRatingRequestDTO> ratings);

    double calculateAverageRating(Roster roster);

    void clearAllRatings();

    void updateRatingsForGame(Integer gameId, String teamColor);

    void checkIfVotingIsComplete(Integer gameId, String teamColor);

    boolean checkVote(Integer playerId);
}