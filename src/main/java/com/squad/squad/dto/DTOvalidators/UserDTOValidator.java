package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.List;

import com.squad.squad.dto.user.UserRoleUpdateRequestDTO;
import com.squad.squad.dto.user.UserUpdateRequestDTO;
import com.squad.squad.enums.Role;
import com.squad.squad.enums.TeamColor;
import org.springframework.stereotype.Component;

import com.squad.squad.dto.user.UserCreateRequestDTO;

@Component
public class UserDTOValidator {

    private final PlayerDTOValidator playerDTOValidator;

    public UserDTOValidator(PlayerDTOValidator playerDTOValidator) {
        this.playerDTOValidator = playerDTOValidator;
    }

    public List<String> validate(UserCreateRequestDTO user) {

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

        if (!playerDTOValidator.validate(user.getPlayerCreateDTO()).isEmpty()) {
            errors.addAll(playerDTOValidator.validate(user.getPlayerCreateDTO()));
        }

        return errors;
    }

    public List<String> validateUpdate(UserUpdateRequestDTO user) {
        List<String> errors = new ArrayList<>();

        // Username validation (Username provided but less than 3 characters)
        if (user.getUsername() != null && user.getUsername().length() < 3) {
            errors.add("Username must be at least 3 characters long if provided.");
        }

        // Password validation (Password provided but not meeting criteria)
        if (user.getPassword() != null && !isValidPassword(user.getPassword())) {
            errors.add("Password must be at least 6 characters long and include uppercase, lowercase, and a number.");
        }

        // Password confirmation validation (Check if passwords match only if both fields are provided)
        if (user.getPassword() != null && user.getPasswordAgain() != null &&
                !user.getPassword().equals(user.getPasswordAgain())) {
            errors.add("Passwords must match.");
        }

        return errors;
    }

    public List<String> validateRoleUpdate(UserRoleUpdateRequestDTO user) {
        List<String> errors = new ArrayList<>();

        if (user.getRole() == null || user.getRole().isEmpty()) {
            errors.add("Role cannot be empty.");
        } else {
            try {
                Role.fromString(user.getRole());
            } catch (RuntimeException e) {
                errors.add("Invalid role: " + user.getRole());
            }
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