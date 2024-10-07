package com.squad.squad.dto.DTOvalidators;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.squad.squad.dto.GameDTO;

@Component
public class GameDTOValidator {

    private final RosterDTOValidator rosterDTOValidator;

    public GameDTOValidator(RosterDTOValidator rosterDTOValidator) {
        this.rosterDTOValidator = rosterDTOValidator;
    }

    public List<String> validate(GameDTO game) {

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

        if (game.getTeamSize() == null || game.getTeamSize() < 5 || game.getTeamSize() > 8) {
            errors.add("Team size cannot be empty and must be between 5 and 8");
        }

        if (game.getRosters() == null || game.getRosters().isEmpty()) {
            errors.add("Rosters cannot be empty");
        } else if (game.getRosters().size() != game.getTeamSize() * 2) {
            errors.add("Roster size must be equal to TeamSize * 2");
        }

        rosterDTOValidator.validate(game.getRosters());

        List<String> rosterErrors = rosterDTOValidator.validate(game.getRosters());
        if (!rosterErrors.isEmpty()) {
            errors.addAll(rosterErrors);
        }

        return errors;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
