package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.squad.squad.dto.UserDTO;

@Component
public class UserDTOValidator {

    private final PlayerDTOValidator playerDTOValidator;

    public UserDTOValidator(PlayerDTOValidator playerDTOValidator) {
        this.playerDTOValidator = playerDTOValidator;
    }

    public List<String> validate(UserDTO user) {

        List<String> errors = new ArrayList<>();


        // Username validation
        if (user.getUsername() == null || user.getUsername().isEmpty() || user.getUsername().trim().isEmpty()) {
            errors.add("Username is required");
        } else if (user.getUsername().length() < 3) {
            errors.add("Username must be at least 3 characters long");
        }

        // Password validation
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            errors.add("Password cannot be empty");
        } else if (!isValidPassword(user.getPassword())) {
            errors.add("Password must be at least 6 characters long and include uppercase, lowercase, and a number");
        }

        // Password confirmation validation
        if (user.getPasswordAgain() == null || user.getPasswordAgain().isEmpty()) {
            errors.add("Please confirm your password");
        } else if (!user.getPassword().equals(user.getPasswordAgain())) {
            errors.add("Passwords must match");
        }

        if (!playerDTOValidator.validate(user.getPlayerDTO()).isEmpty()) {
            errors.addAll(playerDTOValidator.validate(user.getPlayerDTO()));
        }

        return errors;
    }

    private boolean isValidPassword(String password) {

        if (password.length() < 6) {
            return false;
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasUppercase && hasLowercase && hasDigit;
    }

}
