package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.squad.squad.dto.ResetPasswordRequest;

import com.squad.squad.dto.UserDTO;
import com.squad.squad.entity.User;
import com.squad.squad.service.UserService;

import java.util.List;

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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("")
    public ResponseEntity<String> registerUser(@RequestBody User user) {

        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }

        userService.createUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String result = userService.resetPassword(request.getUsername(), request.getNewPassword());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{username}")
    public ResponseEntity<String> updateUserByUsername(@PathVariable String username, @RequestBody User updatedUser) {
        userService.updateUser(username, updatedUser);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted");
    }

}
