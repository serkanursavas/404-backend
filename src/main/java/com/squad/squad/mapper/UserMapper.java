package com.squad.squad.mapper;

import com.squad.squad.dto.user.GetAllUsersDTO;
import com.squad.squad.dto.user.UserResponseDTO;
import com.squad.squad.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "player", target = "player")
    @Mapping(source = "createdAt", target = "createdAt")
    UserResponseDTO userToUserResponseDTO(User user);
    
    @Mapping(source = "createdAt", target = "createdAt")
    List<GetAllUsersDTO> usersToGetAllUsersDTOs(List<User> user);
}