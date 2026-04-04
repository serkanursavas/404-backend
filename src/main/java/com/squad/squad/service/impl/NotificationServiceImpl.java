package com.squad.squad.service.impl;

import com.squad.squad.dto.ExpoPushMessage;
import com.squad.squad.entity.DeviceToken;
import com.squad.squad.enums.GroupRole;
import com.squad.squad.repository.DeviceTokenRepository;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.service.ExpoPushClient;
import com.squad.squad.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final GroupMembershipRepository groupMembershipRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final ExpoPushClient expoPushClient;

    public NotificationServiceImpl(GroupMembershipRepository groupMembershipRepository,
                                   DeviceTokenRepository deviceTokenRepository,
                                   ExpoPushClient expoPushClient) {
        this.groupMembershipRepository = groupMembershipRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.expoPushClient = expoPushClient;
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
}
