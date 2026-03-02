package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.user.*;
import com.squad.squad.entity.User;

public interface UserService {

    boolean existsByUsername(String username);

    AuthResponseDTO createUser(UserCreateRequestDTO user);

    List<GetAllUsersDTO> getAllUsers();

    void updateUser(String username, UserUpdateRequestDTO updatedUser);

    void deleteUser(String username);

    String resetPassword(String username);

    User getUserByUsername(String username);

    AuthResponseDTO login(String username, String password);

    void updateUserRole(String username, UserRoleUpdateRequestDTO roleDTO);
}
