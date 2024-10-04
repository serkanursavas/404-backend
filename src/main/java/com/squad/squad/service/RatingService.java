package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.RatingDTO;
import com.squad.squad.entity.Roster;

public interface RatingService {

    void saveRating(List<RatingDTO> ratings);

    double calculateAvarageRating(Roster roster);

    void clearAllRatings();

    void updateRatingsForGame(Integer gameId, String teamColor);

    void checkIfVotingIsComplete(Integer gameId, String teamColor);
}
