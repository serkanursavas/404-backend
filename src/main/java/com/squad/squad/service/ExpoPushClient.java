package com.squad.squad.service;

import com.squad.squad.dto.ExpoPushMessage;
import com.squad.squad.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpoPushClient {

    private static final Logger log = LoggerFactory.getLogger(ExpoPushClient.class);
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final int BATCH_SIZE = 100;

    private final RestTemplate restTemplate;
    private final DeviceTokenRepository deviceTokenRepository;

    public ExpoPushClient(DeviceTokenRepository deviceTokenRepository) {
        this.restTemplate = new RestTemplate();
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public void sendPushNotifications(List<ExpoPushMessage> messages) {
        for (int i = 0; i < messages.size(); i += BATCH_SIZE) {
            List<ExpoPushMessage> batch = messages.subList(i, Math.min(i + BATCH_SIZE, messages.size()));
            sendBatch(batch);
        }
    }

    @SuppressWarnings("unchecked")
    private void sendBatch(List<ExpoPushMessage> batch) {
        try {
            Map<String, Object> responseBody = restTemplate.postForObject(EXPO_PUSH_URL, batch, Map.class);
            if (responseBody == null) return;

            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            if (data == null) return;

            for (int i = 0; i < data.size() && i < batch.size(); i++) {
                Map<String, Object> ticketOrError = data.get(i);
                String status = (String) ticketOrError.get("status");
                if ("error".equals(status)) {
                    Map<String, Object> details = (Map<String, Object>) ticketOrError.get("details");
                    if (details != null && "DeviceNotRegistered".equals(details.get("error"))) {
                        String token = batch.get(i).getTo();
                        log.warn("Device not registered, deactivating token: {}", token);
                        deviceTokenRepository.findByToken(token).ifPresent(dt -> {
                            dt.setActive(false);
                            deviceTokenRepository.save(dt);
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to send push notifications batch", e);
        }
    }
}
