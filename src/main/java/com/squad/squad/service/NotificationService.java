package com.squad.squad.service;

import com.squad.squad.dto.notification.NotificationResponse;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    void sendPushToSquad(Integer squadId, String title, String body, Map<String, Object> data);

    void sendPushToSquad(Integer squadId, String title, String body, Map<String, Object> data, Integer excludeUserId);

    void sendPushToSquadAdmins(Integer squadId, String title, String body, Map<String, Object> data);

    void sendPushToUser(Integer userId, String title, String body, Map<String, Object> data);

    void saveNotification(Integer userId, Integer squadId, String title, String body, String type, Map<String, Object> data);

    List<NotificationResponse> getNotificationsForUser(Integer userId);

    long getUnreadCount(Integer userId);

    void markAllRead(Integer userId);
}
