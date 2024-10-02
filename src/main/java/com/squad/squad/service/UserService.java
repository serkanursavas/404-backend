package com.squad.squad.service;

import com.squad.squad.dto.UserDTO;
import com.squad.squad.entity.User;
import jakarta.transaction.Transactional;

import java.util.List;

public interface UserService {
    boolean existsByUsername(String username);

    @Transactional
    User registerUser(User user);

    @Transactional
    void deleteByUsername(String username);

    @Transactional
    UserDTO updateUser(String username, User updatedUser);

    List<UserDTO> getAllUsers();

    // Şifre sıfırlama metodu
    String resetPassword(String username, String newPassword);

    void deleteById(Integer id);
}
