package com.squad.squad.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squad.squad.dto.ExpoPushMessage;
import com.squad.squad.dto.notification.NotificationResponse;
import com.squad.squad.entity.DeviceToken;
import com.squad.squad.entity.Notification;
import com.squad.squad.entity.User;
import com.squad.squad.enums.GroupRole;
import com.squad.squad.repository.DeviceTokenRepository;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.repository.NotificationRepository;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.service.ExpoPushClient;
import com.squad.squad.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final GroupMembershipRepository groupMembershipRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ExpoPushClient expoPushClient;
    private final ObjectMapper objectMapper;

    public NotificationServiceImpl(GroupMembershipRepository groupMembershipRepository,
                                   DeviceTokenRepository deviceTokenRepository,
                                   NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   ExpoPushClient expoPushClient,
                                   ObjectMapper objectMapper) {
        this.groupMembershipRepository = groupMembershipRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.expoPushClient = expoPushClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void sendPushToSquad(Integer squadId, String title, String body, Map<String, Object> data) {
        List<Integer> userIds = groupMembershipRepository.findBySquadId(squadId)
                .stream()
                .map(gm -> gm.getUser().getId())
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            log.debug("No members found for squad {}", squadId);
            return;
        }

        String type = data != null ? (String) data.get("type") : null;
        for (Integer userId : userIds) {
            saveNotification(userId, squadId, title, body, type, data);
        }

        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdInAndActiveTrue(userIds);
        if (tokens.isEmpty()) {
            log.debug("No active device tokens for squad {}", squadId);
            return;
        }

        List<ExpoPushMessage> messages = tokens.stream()
                .map(dt -> new ExpoPushMessage(dt.getToken(), title, body, data))
                .collect(Collectors.toList());

        expoPushClient.sendPushNotifications(messages);
    }

    @Override
    @Transactional
    public void sendPushToSquad(Integer squadId, String title, String body, Map<String, Object> data, Integer excludeUserId) {
        List<Integer> userIds = groupMembershipRepository.findBySquadId(squadId)
                .stream()
                .map(gm -> gm.getUser().getId())
                .filter(id -> !id.equals(excludeUserId))
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            log.debug("No members found for squad {} (after excluding actor {})", squadId, excludeUserId);
            return;
        }

        String type = data != null ? (String) data.get("type") : null;
        for (Integer userId : userIds) {
            saveNotification(userId, squadId, title, body, type, data);
        }

        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdInAndActiveTrue(userIds);
        if (tokens.isEmpty()) {
            log.debug("No active device tokens for squad {} (after excluding actor {})", squadId, excludeUserId);
            return;
        }

        List<ExpoPushMessage> messages = tokens.stream()
                .map(dt -> new ExpoPushMessage(dt.getToken(), title, body, data))
                .collect(Collectors.toList());

        expoPushClient.sendPushNotifications(messages);
    }

    @Override
    @Transactional
    public void sendPushToSquadAdmins(Integer squadId, String title, String body, Map<String, Object> data) {
        List<Integer> adminUserIds = groupMembershipRepository.findBySquadIdAndRole(squadId, GroupRole.ADMIN)
                .stream()
                .map(gm -> gm.getUser().getId())
                .collect(Collectors.toList());

        if (adminUserIds.isEmpty()) {
            log.debug("No admin found for squad {}", squadId);
            return;
        }

        String type = data != null ? (String) data.get("type") : null;
        for (Integer userId : adminUserIds) {
            saveNotification(userId, squadId, title, body, type, data);
        }

        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdInAndActiveTrue(adminUserIds);
        if (tokens.isEmpty()) {
            log.debug("No active device tokens for squad {} admins", squadId);
            return;
        }

        List<ExpoPushMessage> messages = tokens.stream()
                .map(dt -> new ExpoPushMessage(dt.getToken(), title, body, data))
                .collect(Collectors.toList());

        expoPushClient.sendPushNotifications(messages);
    }

    @Override
    @Transactional
    public void sendPushToUser(Integer userId, String title, String body, Map<String, Object> data) {
        Integer squadId = data != null && data.get("squadId") instanceof Integer ? (Integer) data.get("squadId") : null;
        String type = data != null ? (String) data.get("type") : null;
        saveNotification(userId, squadId, title, body, type, data);

        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdInAndActiveTrue(List.of(userId));
        if (tokens.isEmpty()) {
            log.debug("No active device tokens for user {}", userId);
            return;
        }
        List<ExpoPushMessage> messages = tokens.stream()
                .map(dt -> new ExpoPushMessage(dt.getToken(), title, body, data))
                .collect(Collectors.toList());
        expoPushClient.sendPushNotifications(messages);
    }

    @Override
    @Transactional
    public void saveNotification(Integer userId, Integer squadId, String title, String body, String type, Map<String, Object> data) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot save notification: user {} not found", userId);
            return;
        }

        String dataJson = null;
        if (data != null) {
            try {
                dataJson = objectMapper.writeValueAsString(data);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize notification data for user {}", userId, e);
            }
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setSquadId(squadId);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setType(type);
        notification.setData(dataJson);
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForUser(Integer userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, PageRequest.of(0, 50))
                .stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getTitle(),
                        n.getBody(),
                        n.getType(),
                        n.getData(),
                        n.getIsRead(),
                        n.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUser_IdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAllRead(Integer userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    private static final int READ_NOTIFICATION_RETENTION_DAYS = 15;

    // Her gece 04:00 — okunmamış bildirimlere hiç dokunmaz, sadece
    // okunmuş ve 15 günden eski olanları temizler.
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupOldReadNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(READ_NOTIFICATION_RETENTION_DAYS);
        int deleted = notificationRepository.deleteReadOlderThan(cutoff);
        if (deleted > 0) {
            log.info("Cleaned up {} read notifications older than {} days", deleted, READ_NOTIFICATION_RETENTION_DAYS);
        }
    }
}
