package com.squad.squad.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.squad.squad.entity.Rating;
import com.squad.squad.entity.Roster;
import com.squad.squad.repository.RatingRepository;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public void saveRating(Rating rating) {
        ratingRepository.save(rating);
    }

    public double calculateAvarageRating(Roster roster) {
        List<Rating> ratings = ratingRepository.findByRoster(roster);
        return ratings.stream().mapToDouble(Rating::getRate).average().orElse(0.0);
    }

}
