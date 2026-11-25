package com.squad.squad.controller;

import com.squad.squad.dto.user.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.squad.squad.dto.DTOvalidators.UserDTOValidator;
import com.squad.squad.service.GroupAuthorizationService;
import com.squad.squad.service.UserService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.security.JwtUtils;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserDTOValidator userDTOValidator;
    private final GroupAuthorizationService authService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, UserDTOValidator userDTOValidator, GroupAuthorizationService authService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.userDTOValidator = userDTOValidator;
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Geçerli jetonla çağrılır, aynı kullanıcı için yeni (30 günlük) jeton döner. Mobil uygulama açılışta ve uzun
     * aradan sonra öne gelince çağırır — düzenli kullanan biri oturumdan düşmez. Süresi dolmuş jetonla çağrılamaz
     * (JwtAuthenticationFilter kimlik kurmaz → 401), o durumda yeniden giriş gerekir.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("token", jwtUtils.generateToken(user.getId(), user.getUsername())));
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        if (request.getIdentifier() == null || request.getIdentifier().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bu alan zorunludur."));
        }
        // Kullanıcı bulunamasa bile 200 dön (enumeration koruması)
        var result = userService.forgotPassword(request.getIdentifier());
        return ResponseEntity.ok(Map.of(
                "message", "Şifre sıfırlama linki email adresinize gönderildi.",
                "username", result.getUsername() != null ? result.getUsername() : "",
                "email", result.getEmail() != null ? result.getEmail() : ""
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPasswordByToken(@RequestBody ResetPasswordRequestDTO request) {
        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token zorunludur."));
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Yeni şifre zorunludur."));
        }
        try {
            userService.resetPasswordByToken(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Şifreniz başarıyla güncellendi."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
