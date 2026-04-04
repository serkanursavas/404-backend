package com.squad.squad.controller;

import com.squad.squad.dto.DeviceTokenRequest;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.DeviceTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final DeviceTokenService deviceTokenService;

    public NotificationController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping("/device-token")
    public ResponseEntity<Void> registerDeviceToken(@RequestBody DeviceTokenRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        deviceTokenService.registerToken(userDetails.getId(), request.getToken(), request.getPlatform());
        return ResponseEntity.ok().build();
    }
}
