package com.squad.squad.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.squad.squad.dto.user.*;
import com.squad.squad.exception.InvalidCredentialsException;
import com.squad.squad.mapper.UserMapper;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.entity.Player;
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

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           PlayerService playerService, AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerService = playerService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
    }

    @Override
    public String login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username.toLowerCase(), password));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer userId = userDetails.getId();

            return jwtUtils.generateToken(userId, username,
                    authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(",")));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public String createUser(UserCreateRequestDTO user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        User savedUser = new User();
        savedUser.setUsername(user.getUsername().toLowerCase());
        savedUser.setPassword(encodedPassword);

        Player player = new Player();
        player.setName(user.getPlayerCreateDTO().getName());
        player.setSurname(user.getPlayerCreateDTO().getSurname());
        player.setPosition(user.getPlayerCreateDTO().getPosition());
        player.setFoot(user.getPlayerCreateDTO().getFoot());

        player.setUser(savedUser);
        savedUser.setPlayer(player);

        userRepository.save(savedUser);

        return jwtUtils.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    @Override
    public List<GetAllUsersDTO> getAllUsers() {
        return userMapper.usersToGetAllUsersDTOs(userRepository.findAll());
    }

    @Override
    public void updateUser(String username, UserUpdateRequestDTO updatedUser) {

        // SecurityContext'ten kullanıcı bilgilerini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // Token içinden kullanıcı adını alır

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
            PlayerDTO player = playerService.getPlayerById(user.getId());

            playerService.softDelete(player);
            userRepository.deleteByUsername(username);
        } else {
            throw new IllegalArgumentException("Userame must be provided.");
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