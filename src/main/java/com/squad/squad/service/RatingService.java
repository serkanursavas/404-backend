package com.squad.squad.service;

import com.squad.squad.entity.Rating;
import com.squad.squad.entity.Roster;

public interface RatingService {

    void saveRating(Rating rating);

    double calculateAvarageRating(Roster roster);

    void clearAllRatings();

}
