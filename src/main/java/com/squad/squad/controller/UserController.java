package com.squad.squad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.squad.squad.dto.ResetPasswordRequest;

import com.squad.squad.dto.UserDTO;
import com.squad.squad.service.UserService;

import java.util.List;

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

    @GetMapping("/getAllUsers")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/createUser")
    public UserDTO createUser(@RequestBody UserDTO user) {
        return userService.createUser(user);
    }

    @PostMapping("/admin/resetPassword")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(request.getUsername(), request.getNewPassword());
    }

    @PutMapping("/updateUserByUsername/{username}")
    public UserDTO updateUserByUsername(@PathVariable String username, @RequestBody UserDTO updatedUser) {
        return userService.updateUser(username, updatedUser);
    }

    @DeleteMapping("/deleteUser/{username}")
    public void deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
    }

}
