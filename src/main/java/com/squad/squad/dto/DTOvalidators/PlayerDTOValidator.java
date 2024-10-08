package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.squad.squad.dto.PlayerDTO;

@Component
public class PlayerDTOValidator {

    public List<String> validate(PlayerDTO player) {

        List<String> errors = new ArrayList<>();

        if (player.getName() == null || player.getName().isEmpty()) {
            errors.add("Player name cannot bi empty");
        }

        if (player.getSurname() == null || player.getSurname().isEmpty()) {
            errors.add("Player surname cannot bi empty");
        }

        if (player.getFoot() == null || player.getFoot().isEmpty()) {
            errors.add("Player foot info cannot bi empty");
        }

        if (player.getPosition() == null || player.getPosition().isEmpty()) {
            errors.add("Player position info cannot bi empty");
        }

        return errors;
    }
}
