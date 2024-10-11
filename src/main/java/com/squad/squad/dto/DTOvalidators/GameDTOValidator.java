package com.squad.squad.dto.DTOvalidators;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.squad.squad.dto.game.GameCreateRequestDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import org.springframework.stereotype.Component;

import com.squad.squad.dto.GameDTO;

@Component
public class GameDTOValidator {

    private final RosterDTOValidator rosterDTOValidator;

    public GameDTOValidator(RosterDTOValidator rosterDTOValidator) {
        this.rosterDTOValidator = rosterDTOValidator;
    }

    public List<String> validate(GameCreateRequestDTO game) {

        List<String> errors = new ArrayList<>();

        if (isNullOrEmpty(game.getLocation())) {
            errors.add("Location cannot be empty");
        }

        if (isNullOrEmpty(game.getWeather())) {
            errors.add("Weather info cannot be empty");
        }

        if (game.getDateTime() == null) {
            errors.add("Date cannot be empty");
        } else if (game.getDateTime().isBefore(LocalDateTime.now())) {
            errors.add("Game date cannot be in the past");
        }

        if (game.getTeamSize() == null || game.getTeamSize() < 6 || game.getTeamSize() > 11) {
            errors.add("Team size cannot be empty and must be between 6 and 11");
        }

        if (game.getRosters() == null || game.getRosters().isEmpty()) {
            errors.add("Rosters cannot be empty");
        } else if (game.getRosters().size() != game.getTeamSize() * 2) {
            errors.add("Roster size must be equal to " + game.getTeamSize() * 2);
        }

        rosterDTOValidator.validate(game.getRosters());

        List<String> rosterErrors = rosterDTOValidator.validate(game.getRosters());
        if (!rosterErrors.isEmpty()) {
            errors.addAll(rosterErrors);
        }

        return errors;
    }

    public List<String> updateValidate(GameUpdateRequestDTO game) {
        List<String> errors = new ArrayList<>();

        if (game.getId() == null) {
            errors.add("Game ID cannot be null for update operations.");
        }

        if (game.getLocation() != null && game.getLocation().trim().isEmpty()) {
            errors.add("Location cannot be empty if provided.");
        }

        if (game.getWeather() != null && game.getWeather().trim().isEmpty()) {
            errors.add("Weather info cannot be empty if provided.");
        }

        if (game.getDateTime() != null && game.getDateTime().isBefore(LocalDateTime.now())) {
            errors.add("Game date cannot be in the past unless specifically updating the date.");
        }

        if ((game.getTeamSize() != null && (game.getRosters() == null || game.getRosters().isEmpty())) ||
                (game.getRosters() != null && !game.getRosters().isEmpty() && game.getTeamSize() == null)) {
            errors.add("Both team size and rosters must be provided together if either is present.");
        }

        if (game.getTeamSize() != null && game.getRosters() != null && !game.getRosters().isEmpty()) {
            if (game.getTeamSize() < 6 || game.getTeamSize() > 11) {
                errors.add("Team size must be between 6 and 11.");
            }

            if (game.getRosters().size() != game.getTeamSize() * 2) {
                errors.add("Roster size must be equal to " + game.getTeamSize() * 2);
            }

            List<String> rosterErrors = rosterDTOValidator.validateUpdate(game.getRosters());
            if (!rosterErrors.isEmpty()) {
                errors.addAll(rosterErrors);
            }
        }

        return errors;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}