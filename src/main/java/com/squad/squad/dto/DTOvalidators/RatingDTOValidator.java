package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.squad.squad.dto.rating.AddRatingRequestDTO;
import org.springframework.stereotype.Component;

import com.squad.squad.dto.RatingDTO;

@Component
public class RatingDTOValidator {

    public List<String> validate(List<AddRatingRequestDTO> ratings) {
        List<String> errors = new ArrayList<>();
        Set<Integer> votedRosterIds = new HashSet<>();

        // Boş liste veya null kontrolü
        if (ratings == null || ratings.isEmpty()) {
            errors.add("Rating list cannot be null or empty.");
            return errors;
        }

        for (AddRatingRequestDTO rating : ratings) {

            // Player ID null ve negatif değer kontrolü
            if (rating.getPlayerId() == null || rating.getPlayerId() <= 0) {
                errors.add("Player ID must be a positive number.");
            }

            // Rate null ve geçerli aralık kontrolü
            if (rating.getRate() == null) {
                errors.add("Rate cannot be null.");
            } else if (rating.getRate() < 1 || rating.getRate() > 10) {
                errors.add("Rate must be between 1 and 10.");
            }

            // Roster ID null ve negatif değer kontrolü
            if (rating.getRosterId() == null || rating.getRosterId() <= 0) {
                errors.add("Roster ID must be a positive number.");
            } else {
                // Aynı rosterId'ye birden fazla oy verilmesini engelle
                if (!votedRosterIds.add(rating.getRosterId())) {
                    errors.add("Duplicate vote detected for roster ID: " + rating.getRosterId());
                }
            }
        }

        return errors;
    }
}