package com.squad.squad.service.impl;

import org.springframework.stereotype.Service;

import com.squad.squad.entity.Rating;
import com.squad.squad.entity.Roster;
import com.squad.squad.exception.InvalidRatingException;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.service.RatingService;

import jakarta.transaction.Transactional;

@Service
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;

    public RatingServiceImpl(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Override
    @Transactional
    public void saveRating(Rating rating) {
        if (rating.getRate() < 1 || rating.getRate() > 10) {
            throw new InvalidRatingException("Rating must be between 1 and 10");
        }
        ratingRepository.save(rating);
    }

    @Override
    public double calculateAvarageRating(Roster roster) {
        Double average = ratingRepository.findAverageRatingByRoster(roster);
        return average != null ? average : 0.0;
    }

    @Override
    @Transactional
    public void clearAllRatings() {
        ratingRepository.deleteAll();
    }
}
