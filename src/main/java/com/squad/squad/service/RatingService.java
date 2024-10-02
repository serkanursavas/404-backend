package com.squad.squad.service;

import org.springframework.stereotype.Service;

import com.squad.squad.entity.Rating;
import com.squad.squad.entity.Roster;
import com.squad.squad.exception.InvalidRatingException;
import com.squad.squad.repository.RatingRepository;

import jakarta.transaction.Transactional;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Transactional
    public void saveRating(Rating rating) {
        if (rating.getRate() < 1 || rating.getRate() > 10) {
            throw new InvalidRatingException("Rating must be between 1 and 10");
        }
        ratingRepository.save(rating);
    }

    public double calculateAvarageRating(Roster roster) {
        Double average = ratingRepository.findAverageRatingByRoster(roster);
        return average != null ? average : 0.0;
    }

    @Transactional
    public void clearAllRatings() {
        ratingRepository.deleteAll();
    }

}
