package com.squad.squad.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.squad.squad.dto.squad.SquadSummaryDTO;
import com.squad.squad.dto.user.*;
import com.squad.squad.dto.user.ForgotPasswordResultDTO;
import com.squad.squad.entity.GroupMembership;
import com.squad.squad.exception.InvalidCredentialsException;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.repository.SquadRequestRepository;
import com.squad.squad.repository.JoinRequestRepository;
import com.squad.squad.enums.RequestStatus;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.entity.PasswordResetToken;
import com.squad.squad.entity.User;
import com.squad.squad.exception.UserNotFoundException;
import com.squad.squad.repository.PasswordResetTokenRepository;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.service.EmailService;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.SquadService;
import com.squad.squad.service.UserService;

import org.springframework.beans.factory.annotation.Value;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerService playerService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final GroupMembershipRepository groupMembershipRepository;
    private final SquadRequestRepository squadRequestRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final SquadService squadService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           PlayerService playerService, AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils,
                           GroupMembershipRepository groupMembershipRepository,
                           SquadRequestRepository squadRequestRepository,
                           JoinRequestRepository joinRequestRepository,
                           SquadService squadService,
                           EmailService emailService,
                           PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerService = playerService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.groupMembershipRepository = groupMembershipRepository;
        this.squadRequestRepository = squadRequestRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.squadService = squadService;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    @Transactional
    public AuthResponseDTO login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username.toLowerCase(), password));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer userId = userDetails.getId();

            String token = jwtUtils.generateToken(userId, username);

            // Build squad list
            List<GroupMembership> memberships = groupMembershipRepository.findByUserId(userId);
            List<SquadSummaryDTO> squads = memberships.stream()
                    .map(m -> {
                        Integer squadId = m.getSquad().getId();
                        String adminName = squadService.getAdminPlayerNameForSquad(squadId);
                        int memberCount = squadService.getMemberCountForSquad(squadId);
                        return new SquadSummaryDTO(squadId, m.getSquad().getName(), m.getRole().name(), adminName, memberCount);
                    })
                    .collect(Collectors.toList());

            // Count pending requests
            long pendingSquadRequests = squadRequestRepository.findByRequestedByUserId(userId).stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING).count();
            long pendingJoinRequests = joinRequestRepository.findByUserId(userId).stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING).count();

            User user = userRepository.findById(userId).orElse(null);

            AuthResponseDTO response = new AuthResponseDTO();
            response.setToken(token);
            response.setSquads(squads);
            response.setSuperAdmin(user != null && user.isSuperAdmin());
            response.setPendingRequestCount((int) (pendingSquadRequests + pendingJoinRequests));
            response.setHasEmail(user != null && user.getEmail() != null && !user.getEmail().isEmpty());
            response.setEmail(user != null ? user.getEmail() : null);

            return response;
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthResponseDTO createUser(UserCreateRequestDTO userDto) {
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());

        User savedUser = new User();
        savedUser.setUsername(userDto.getUsername().toLowerCase());
        savedUser.setPassword(encodedPassword);
        savedUser.setEmail(userDto.getEmail() != null ? userDto.getEmail().toLowerCase().trim() : null);

        // No longer create a Player here - player is created when joining a squad
        userRepository.save(savedUser);

        String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getUsername());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setSquads(List.of());
        response.setSuperAdmin(false);
        response.setPendingRequestCount(0);
        response.setHasEmail(savedUser.getEmail() != null && !savedUser.getEmail().isEmpty());
        response.setEmail(savedUser.getEmail());

        return response;
    }

    @Override
    public void updateUser(String username, UserUpdateRequestDTO updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (!currentUsername.equals(username.toLowerCase())) {
            throw new SecurityException("You can only update your own profile.");
        }

        User existingUser = getUserByUsername(username);

        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(updatedUser.getPassword());
            existingUser.setPassword(encodedPassword);
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().trim().isEmpty()) {
            existingUser.setEmail(updatedUser.getEmail().toLowerCase().trim());
        }

        userRepository.save(existingUser);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public ForgotPasswordResultDTO forgotPassword(String identifier) {
        // identifier: email veya kullanıcı adı — bulunamasa bile aynı yanıtı ver (enumeration koruması)
        String trimmed = identifier.toLowerCase().trim();
        Optional<User> found = userRepository.findByEmail(trimmed);
        if (found.isEmpty()) {
            found = userRepository.findUserByUsername(trimmed);
        }
        if (found.isPresent()) {
            User user = found.get();

            // Önceki tokenları sil
            passwordResetTokenRepository.deleteByUserId(user.getId());

            // Yeni token oluştur
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                return new ForgotPasswordResultDTO(user.getUsername(), null);
            }

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken.getToken();
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

            return new ForgotPasswordResultDTO(user.getUsername(), user.getEmail());
        }
        return new ForgotPasswordResultDTO(null, null);
    }

    @Override
    @Transactional
    public void resetPasswordByToken(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz veya süresi dolmuş token."));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Bu token daha önce kullanılmış.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token süresi dolmuş. Lütfen yeni bir şifre sıfırlama isteği gönderin.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
