package com.squad.squad.controller;

import com.squad.squad.config.RLSHelper;
import com.squad.squad.context.TenantContext;
import com.squad.squad.dto.user.*;
import com.squad.squad.entity.GroupMembership;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.util.RLSContextManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
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
    private final RLSHelper rlsHelper;
    private final RLSContextManager rlsContextManager;
    private final GroupMembershipRepository groupMembershipRepository;

    public UserController(UserService userService, UserDTOValidator userDTOValidator, RLSHelper rlsHelper, RLSContextManager rlsContextManager, GroupMembershipRepository groupMembershipRepository) {
        this.userService = userService;
        this.userDTOValidator = userDTOValidator;
        this.rlsHelper = rlsHelper;
        this.rlsContextManager = rlsContextManager;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @GetMapping("/admin/getAllUsers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<GetAllUsersDTO>> getAllUsers() {
        List<GetAllUsersDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/createUser")
    @Transactional
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequestDTO user) {
        try {

            // Context'i Group 0 için set et (pending users)
            TenantContext.setTenantId(0);
            rlsContextManager.setTenantContext(0);

            List<String> errors = userDTOValidator.validate(user);
            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
            }

            boolean userExists = userService.existsByUsername(user.getUsername());
            if (userExists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Username already exists. Please choose a different username.");
            }

            String token = userService.createUser(user);
            AuthResponseDTO response = new AuthResponseDTO();
            response.setToken(token);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("User creation failed: " + e.getMessage());
        } finally {
            // Context temizle
            try {
                TenantContext.clear();
                rlsContextManager.clearContext();
            } catch (Exception e) {
            }
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        String token = userService.login(userLoginRequestDTO.getUsername(), userLoginRequestDTO.getPassword());
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/resetPassword/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable String username) {
        String result = userService.resetPassword(username);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateProfile/{username}")
    @PreAuthorize("isAuthenticated() and @userService.canUserAccessUserData(authentication.principal, #username)")
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserByUsername(@PathVariable String username, @RequestBody UserRoleUpdateRequestDTO updatedRole) {
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully with username: " + username.toLowerCase());
    }

    @GetMapping("/is-group-admin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isGroupAdmin() {
        try {
            CustomUserDetails currentUser = getCurrentUser();
            boolean isGroupAdmin = userService.isGroupAdmin(currentUser.getId());
            return ResponseEntity.ok(isGroupAdmin);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    // Helper method
    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}