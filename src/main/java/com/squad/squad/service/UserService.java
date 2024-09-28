package com.squad.squad.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.UserDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.User;
import com.squad.squad.repository.PlayerRepository;
import com.squad.squad.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerRepository playerRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            PlayerRepository playerRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerRepository = playerRepository;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public User registerUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);

        Player player = new Player();
        player.setId(savedUser.getId());
        playerRepository.save(player);

        return savedUser;
    }

    @Transactional
    public void deleteByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Player player = playerRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        player.setActive(false);

        playerRepository.save(player);
        userRepository.deleteByUsername(username);
    }

    @Transactional
    public UserDTO updateUser(String username, User updatedUser) {

        Optional<User> existingUserOptional = userRepository.findByUsername(username);

        if (existingUserOptional.isPresent()) {

            User existingUser = existingUserOptional.get();

            if (userRepository.existsByUsername(updatedUser.getUsername())) {
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
        } else {
            throw new RuntimeException("User not found with username: " + username);
        }

    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getRole()))
                .collect(Collectors.toList());
    }

    // Şifre sıfırlama metodu
    public String resetPassword(String username, String newPassword) {
        // Kullanıcıyı username'e göre bul
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Yeni şifreyi hash'le
            String encodedPassword = passwordEncoder.encode(newPassword);

            // Şifreyi güncelle
            user.setPassword(encodedPassword);

            // Kullanıcıyı veritabanına kaydet
            userRepository.save(user);

            return "Password reset successfully for user: " + username;
        } else {
            return "User not found with username: " + username;
        }
    }
}
