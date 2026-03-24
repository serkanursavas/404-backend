package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.user.AuthResponseDTO;
import com.squad.squad.dto.user.ForgotPasswordResultDTO;
import com.squad.squad.dto.user.GetAllUsersDTO;
import com.squad.squad.dto.user.UserCreateRequestDTO;
import com.squad.squad.dto.user.UserUpdateRequestDTO;
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

    ForgotPasswordResultDTO forgotPassword(String identifier);

    void resetPasswordByToken(String token, String newPassword);
}
