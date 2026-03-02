package com.squad.squad.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.squad.squad.dto.squad.SquadSummaryDTO;
import com.squad.squad.dto.user.*;
import com.squad.squad.entity.GroupMembership;
import com.squad.squad.exception.InvalidCredentialsException;
import com.squad.squad.mapper.UserMapper;
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
import com.squad.squad.entity.User;
import com.squad.squad.exception.UserNotFoundException;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerService playerService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final GroupMembershipRepository groupMembershipRepository;
    private final SquadRequestRepository squadRequestRepository;
    private final JoinRequestRepository joinRequestRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           PlayerService playerService, AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils, UserMapper userMapper,
                           GroupMembershipRepository groupMembershipRepository,
                           SquadRequestRepository squadRequestRepository,
                           JoinRequestRepository joinRequestRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerService = playerService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.groupMembershipRepository = groupMembershipRepository;
        this.squadRequestRepository = squadRequestRepository;
        this.joinRequestRepository = joinRequestRepository;
    }

    @Override
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
                    .map(m -> new SquadSummaryDTO(m.getSquad().getId(), m.getSquad().getName(), m.getRole().name()))
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

        // No longer create a Player here - player is created when joining a squad
        userRepository.save(savedUser);

        String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getUsername());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setSquads(List.of());
        response.setSuperAdmin(false);
        response.setPendingRequestCount(0);

        return response;
    }

    @Override
    public List<GetAllUsersDTO> getAllUsers() {
        return userMapper.usersToGetAllUsersDTOs(userRepository.findAll());
    }

    @Override
    public void updateUser(String username, UserUpdateRequestDTO updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (!currentUsername.equals(username.toLowerCase())) {
            throw new InvalidCredentialsException("You can only update your own profile.");
        }

        User existingUser = getUserByUsername(username);

        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(updatedUser.getPassword());
            existingUser.setPassword(encodedPassword);
        }

        userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(String username) {
        if (username != null) {
            User user = getUserByUsername(username);
            // Player deletion is now handled per-squad via membership
            userRepository.deleteByUsername(username);
        } else {
            throw new IllegalArgumentException("Username must be provided.");
        }
    }

    @Override
    public String resetPassword(String username) {
        User user = getUserByUsername(username);

        String encodedPassword = passwordEncoder.encode("reset" + user.getUsername());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        return "Password reset to reset" + username + " successfully for user: " + username;
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
    public void updateUserRole(String username, UserRoleUpdateRequestDTO roleDTO) {
        User user = getUserByUsername(username);
        user.setRole(roleDTO.getRole().toUpperCase());
        userRepository.save(user);
    }
}
