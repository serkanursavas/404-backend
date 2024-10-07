package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.squad.squad.dto.ResetPasswordRequest;

import com.squad.squad.dto.UserDTO;
import com.squad.squad.dto.DTOvalidators.UserDTOValidator;
import com.squad.squad.service.UserService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

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
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) {
        List<String> errors = userDTOValidator.validate(user);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
        }

        boolean userExists = userService.existsByUsername(user.getUsername());
        if (userExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already exists. Please choose a different username.");
        }

        UserDTO createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/admin/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        String result = userService.resetPassword(request.getUsername(), request.getNewPassword());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateUserByUsername/{username}")
    public ResponseEntity<?> updateUserByUsername(@PathVariable String username, @RequestBody UserDTO updatedUser) {
        List<String> errors = userDTOValidator.validate(updatedUser);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        UserDTO updated = userService.updateUser(username, updatedUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/deleteUser/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

}
