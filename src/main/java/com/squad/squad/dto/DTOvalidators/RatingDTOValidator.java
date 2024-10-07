package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.squad.squad.dto.RatingDTO;

@Component
public class RatingDTOValidator {

    public List<String> validate(List<RatingDTO> ratings) {

        List<String> errors = new ArrayList<>();
        Set<Integer> rosterIds = new HashSet<>();

        if (ratings == null || ratings.isEmpty()) {
            errors.add("Ratings list cannot be empty");
            return errors;
        }

        for (RatingDTO rating : ratings) {

            if (rating.getPlayerId() == null) {
                errors.add("Player ID cannot be null");
            }

            if (rating.getRate() == null) {
                errors.add("Rate cannot be empty");
            } else if (rating.getRate() > 10 || rating.getRate() < 1) {
                errors.add("Rate must be between 1 and 10. Invalid rate for player ID: " + rating.getPlayerId());
            }

            if (rating.getRosterId() == null) {
                errors.add("Roster ID cannot be null");
            } else if (!rosterIds.add(rating.getRosterId())) {
                errors.add("Duplicate rating for roster ID: " + rating.getRosterId());
            }

        }

        return errors;

    }

}
