package com.squad.squad.controller;

import com.squad.squad.dto.user.*;
import org.springframework.web.bind.annotation.*;

import com.squad.squad.dto.DTOvalidators.UserDTOValidator;
import com.squad.squad.service.GroupAuthorizationService;
import com.squad.squad.service.UserService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserDTOValidator userDTOValidator;
    private final GroupAuthorizationService authService;

    public UserController(UserService userService, UserDTOValidator userDTOValidator, GroupAuthorizationService authService) {
        this.userService = userService;
        this.userDTOValidator = userDTOValidator;
        this.authService = authService;
    }

    @GetMapping("/admin/getAllUsers")
    public ResponseEntity<List<GetAllUsersDTO>> getAllUsers() {
        authService.requireSuperAdmin();
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

        AuthResponseDTO response = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        AuthResponseDTO response = userService.login(userLoginRequestDTO.getUsername(), userLoginRequestDTO.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/resetPassword/{username}")
    public ResponseEntity<?> resetPassword(@PathVariable String username) {
        authService.requireSuperAdmin();
        String result = userService.resetPassword(username);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateProfile/{username}")
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

    @PutMapping("/admin/updateUserRole/{username}")
    public ResponseEntity<?> updateUserByUsername(@PathVariable String username, @RequestBody UserRoleUpdateRequestDTO updatedRole) {
        authService.requireSuperAdmin();
        List<String> errors = userDTOValidator.validateRoleUpdate(updatedRole);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        boolean userExists = userService.existsByUsername(username);
        if (!userExists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found.");
        }

        userService.updateUserRole(username, updatedRole);
        return ResponseEntity.ok("User updated successfully.");
    }

    @DeleteMapping("/admin/deleteUser/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        authService.requireSuperAdmin();
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully with username: " + username.toLowerCase());
    }
}
