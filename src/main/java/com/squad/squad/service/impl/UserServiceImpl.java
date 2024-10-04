package com.squad.squad.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.UserDTO;
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

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            PlayerService playerService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerService = playerService;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        User savedUser = new User();
        savedUser.setPassword(encodedPassword);
        savedUser.setRole(user.getRole());
        savedUser.setUsername(user.getUsername());
        userRepository.save(savedUser);

        Player player = new Player();
        player.setUser(savedUser);

        playerService.createPlayer(player);

        return user;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(String username, User updatedUser) {

        User existingUser = getUserByUsername(username);

        if (!existingUser.getUsername().equals(updatedUser.getUsername())
                && userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new RuntimeException("Username already taken: " + username);
        }

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setRole(updatedUser.getRole());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(updatedUser.getPassword());
            existingUser.setPassword(hashedPassword);
        }

        userRepository.save(existingUser);
        return new UserDTO(existingUser.getId(), existingUser.getUsername(), existingUser.getRole());

    }

    @Override
    @Transactional
    public void deleteUser(String username) {
        if (username != null) {
            User user = getUserByUsername(username);
            Player player = playerService.getPlayerById(user.getId());

            player.setActive(false);
            playerService.updatePlayer(player);
            userRepository.deleteByUsername(username);
        } else {
            throw new IllegalArgumentException("Userame must be provided.");
        }
    }

    @Override
    public String resetPassword(String username, String newPassword) {
        User user = getUserByUsername(username);

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        return "Password reset successfully for user: " + username;
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
    public void deleteById(Integer id) {
        try {
            userRepository.deleteById(id);
        } catch (Exception e) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
    }

}
