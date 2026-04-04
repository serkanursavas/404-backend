package com.squad.squad.service;

public interface DeviceTokenService {

    void registerToken(Integer userId, String token, String platform);
}
