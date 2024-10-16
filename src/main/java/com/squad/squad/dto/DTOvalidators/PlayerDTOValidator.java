package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.List;

import com.squad.squad.dto.PlayerCreateDTO;
import com.squad.squad.dto.player.PlayerUpdateRequestDTO;
import org.springframework.stereotype.Component;

import com.squad.squad.dto.PlayerDTO;

@Component
public class PlayerDTOValidator {

    public List<String> validate(PlayerCreateDTO player) {

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

    public List<String> validateUpdate(PlayerUpdateRequestDTO player) {
        List<String> errors = new ArrayList<>();

        // Eğer isim boşsa veya null ise hata ekle
        if (player.getName() != null && player.getName().isEmpty()) {
            errors.add("Player name cannot be empty if provided.");
        }

        // Soyadı kontrol et
        if (player.getSurname() != null && player.getSurname().isEmpty()) {
            errors.add("Player surname cannot be empty if provided.");
        }

        // Ayak bilgisi kontrol et
        if (player.getFoot() != null && player.getFoot().isEmpty()) {
            errors.add("Player foot info cannot be empty if provided.");
        }

        // Pozisyon bilgisi kontrol et
        if (player.getPosition() != null && player.getPosition().isEmpty()) {
            errors.add("Player position info cannot be empty if provided.");
        }

        return errors;
    }
}