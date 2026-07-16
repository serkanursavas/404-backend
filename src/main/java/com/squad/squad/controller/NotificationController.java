package com.squad.squad.controller;

import com.squad.squad.dto.DeviceTokenRequest;
import com.squad.squad.dto.notification.NotificationResponse;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.DeviceTokenService;
import com.squad.squad.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final DeviceTokenService deviceTokenService;
    private final NotificationService notificationService;

    public NotificationController(DeviceTokenService deviceTokenService, NotificationService notificationService) {
        this.deviceTokenService = deviceTokenService;
        this.notificationService = notificationService;
    }

    @PostMapping("/device-token")
    public ResponseEntity<Void> registerDeviceToken(@RequestBody DeviceTokenRequest request) {
        Integer userId = getCurrentUserId();
        deviceTokenService.registerToken(userId, request.getToken(), request.getPlatform());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Integer userId = getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        Integer userId = getCurrentUserId();
        notificationService.markAllRead(userId);
        return ResponseEntity.ok().build();
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
