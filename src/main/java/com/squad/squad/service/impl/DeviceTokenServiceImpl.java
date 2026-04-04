package com.squad.squad.service.impl;

import com.squad.squad.entity.DeviceToken;
import com.squad.squad.entity.User;
import com.squad.squad.exception.NotFoundException;
import com.squad.squad.repository.DeviceTokenRepository;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.service.DeviceTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    public DeviceTokenServiceImpl(DeviceTokenRepository deviceTokenRepository, UserRepository userRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void registerToken(Integer userId, String token, String platform) {
        DeviceToken deviceToken = deviceTokenRepository.findByToken(token)
                .orElse(null);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Deactivate all other active tokens for this user
        deviceTokenRepository.findByUserIdAndActiveTrueAndTokenNot(userId, token)
                .forEach(old -> {
                    old.setActive(false);
                    deviceTokenRepository.save(old);
                });

        if (deviceToken != null) {
            deviceToken.setUser(user);
            deviceToken.setPlatform(platform);
            deviceToken.setActive(true);
            deviceTokenRepository.save(deviceToken);
        } else {
            DeviceToken newToken = new DeviceToken();
            newToken.setUser(user);
            newToken.setToken(token);
            newToken.setPlatform(platform);
            deviceTokenRepository.save(newToken);
        }
    }
}
