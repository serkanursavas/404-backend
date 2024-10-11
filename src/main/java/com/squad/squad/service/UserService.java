package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.user.GetAllUsersDTO;
import com.squad.squad.dto.user.UserCreateRequestDTO;
import com.squad.squad.dto.user.UserResponseDTO;
import com.squad.squad.dto.user.UserUpdateRequestDTO;
import com.squad.squad.entity.User;
import org.springframework.http.ResponseEntity;

public interface UserService {

    boolean existsByUsername(String username);

    UserResponseDTO createUser(UserCreateRequestDTO user);

    List<GetAllUsersDTO> getAllUsers();

    void updateUser(String username, UserUpdateRequestDTO updatedUser);

    void deleteUser(String username);

    String resetPassword(String username);

    User getUserByUsername(String username);

    String login(String username, String password);
}