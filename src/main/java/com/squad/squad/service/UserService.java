package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.UserDTO;
import com.squad.squad.entity.User;

public interface UserService {

    boolean existsByUsername(String username);

    UserDTO createUser(UserDTO user);

    List<UserDTO> getAllUsers();

    UserDTO updateUser(String username, UserDTO updatedUser);

    void deleteUser(String username);

    String resetPassword(String username, String newPassword);

    User getUserByUsername(String username);

    void deleteById(Integer id);

}
