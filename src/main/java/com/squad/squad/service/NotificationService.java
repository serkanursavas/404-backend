package com.squad.squad.service;

import java.util.Map;

public interface NotificationService {

    void sendPushToSquad(Integer squadId, String title, String body, Map<String, Object> data);

    void sendPushToSquad(Integer squadId, String title, String body, Map<String, Object> data, Integer excludeUserId);

    void sendPushToSquadAdmins(Integer squadId, String title, String body, Map<String, Object> data);

    void sendPushToUser(Integer userId, String title, String body, Map<String, Object> data);

    void sendPushToUsers(java.util.Collection<Integer> userIds, String title, String body, Map<String, Object> data);
}
