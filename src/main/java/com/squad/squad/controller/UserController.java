package com.squad.squad.controller;

import com.squad.squad.dto.user.*;
import com.squad.squad.entity.User;
import org.springframework.web.bind.annotation.*;

import com.squad.squad.dto.DTOvalidators.UserDTOValidator;
import com.squad.squad.service.UserService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserDTOValidator userDTOValidator;

    public UserController(UserService userService, UserDTOValidator userDTOValidator) {
        this.userService = userService;
        this.userDTOValidator = userDTOValidator;
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<GetAllUsersDTO>> getAllUsers() {
        List<GetAllUsersDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequestDTO user) {
        List<String> errors = userDTOValidator.validate(user);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
        }

        boolean userExists = userService.existsByUsername(user.getUsername());
        if (userExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already exists. Please choose a different username.");
        }

        UserResponseDTO createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        String token = userService.login(userLoginRequestDTO.getUsername(), userLoginRequestDTO.getPassword());
        UserLoginResponseDTO response = new UserLoginResponseDTO();
        response.setToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/resetPassword/{username}")
    public ResponseEntity<?> resetPassword(@PathVariable String username) {
        String result = userService.resetPassword(username);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/admin/updateUserByUsername/{username}")
    public ResponseEntity<?> updateUserByUsername(@PathVariable String username, @RequestBody UserUpdateRequestDTO updatedUser) {
        List<String> errors = userDTOValidator.validateUpdate(updatedUser);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        boolean userExists = userService.existsByUsername(username);
        if (!userExists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found.");
        }

        if (!username.equals(updatedUser.getUsername())
                && userService.existsByUsername(updatedUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already taken: " + updatedUser.getUsername());
        }

        userService.updateUser(username, updatedUser);
        return ResponseEntity.ok("User updated successfully.");
    }

    @DeleteMapping("/admin/deleteUser/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully with username: " + username);
    }
}