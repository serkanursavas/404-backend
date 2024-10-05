package com.squad.squad.service.impl;

import com.squad.squad.entity.User;
import com.squad.squad.exception.UserNotFoundException;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.service.UserEntityService;
import org.springframework.stereotype.Service;

@Service
public class UserEntityServiceImpl implements UserEntityService {

    private final UserRepository userRepository;

    public UserEntityServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
}
