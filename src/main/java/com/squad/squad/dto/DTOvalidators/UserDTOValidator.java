package com.squad.squad.dto.DTOvalidators;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.squad.squad.dto.UserDTO;

@Component
public class UserDTOValidator {

    public List<String> validate(UserDTO user) {

        List<String> errors = new ArrayList<>();

        if (user.getUsername() == null || user.getUsername().isEmpty() || user.getUsername().trim() == "") {
            errors.add("Username is required");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            errors.add("Password cannot be empty");
        } else if (isValidPassword(user.getPassword())) {
            errors.add(
                    "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character");
        }

        return errors;
    }

    private boolean isValidPassword(String password) {

        if (password.length() < 8) {
            return false;
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChar = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

}
