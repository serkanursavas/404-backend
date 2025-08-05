package com.squad.squad.mapper;

import com.squad.squad.dto.user.GetAllUsersDTO;
import com.squad.squad.dto.user.UserResponseDTO;
import com.squad.squad.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponseDTO userToUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setUsername(user.getUsername());
        userResponseDTO.setRole(user.getRole());
        userResponseDTO.setCreatedAt(user.getCreatedAt());

        // Set player information if available
        if (user.getPlayer() != null) {
            // Create PlayerDTO and set it
            com.squad.squad.dto.PlayerDTO playerDTO = new com.squad.squad.dto.PlayerDTO();
            playerDTO.setId(user.getPlayer().getId());
            playerDTO.setName(user.getPlayer().getName());
            playerDTO.setSurname(user.getPlayer().getSurname());
            userResponseDTO.setPlayer(playerDTO);
        }

        return userResponseDTO;
    }

    public List<GetAllUsersDTO> usersToGetAllUsersDTOs(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::userToGetAllUsersDTO)
                .collect(Collectors.toList());
    }

    private GetAllUsersDTO userToGetAllUsersDTO(User user) {
        if (user == null) {
            return null;
        }

        GetAllUsersDTO getAllUsersDTO = new GetAllUsersDTO();
        getAllUsersDTO.setId(user.getId());
        getAllUsersDTO.setUsername(user.getUsername());
        getAllUsersDTO.setSystemRole(user.getRole());
        getAllUsersDTO.setGroupId(user.getGroupId());
        getAllUsersDTO.setCreatedAt(user.getCreatedAt());

        return getAllUsersDTO;
    }
}