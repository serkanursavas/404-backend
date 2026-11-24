package com.squad.squad.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squad.squad.dto.ExpoPushMessage;
import com.squad.squad.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExpoPushClient {

    private static final Logger log = LoggerFactory.getLogger(ExpoPushClient.class);
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final int BATCH_SIZE = 100;
    private static final String KARISIK_PROJE = "PUSH_TOO_MANY_EXPERIENCE_IDS";

    private final RestTemplate restTemplate;
    private final DeviceTokenRepository deviceTokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExpoPushClient(DeviceTokenRepository deviceTokenRepository) {
        this(deviceTokenRepository, new RestTemplate());
    }

    ExpoPushClient(DeviceTokenRepository deviceTokenRepository, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public void sendPushNotifications(List<ExpoPushMessage> messages) {
        for (int i = 0; i < messages.size(); i += BATCH_SIZE) {
            List<ExpoPushMessage> batch = messages.subList(i, Math.min(i + BATCH_SIZE, messages.size()));
            sendBatch(batch, true);
        }
    }

    /**
     * Expo bir istekteki bütün jetonların AYNI projeye (experience) ait olmasını ister; farklı projelerin
     * jetonları karışınca (eski 404 Squad uygulaması + FUTZA) isteğin tamamı 400 PUSH_TOO_MANY_EXPERIENCE_IDS
     * ile reddedilir ve kadroda kimseye bildirim gitmez. Bu durumda hata gövdesindeki proje → jeton listesine
     * göre mesajlar gruplanıp her proje ayrı istekle gönderilir (tek seviye; grup içinde tekrar bölünmez).
     */
    @SuppressWarnings("unchecked")
    private void sendBatch(List<ExpoPushMessage> batch, boolean projeyeGoreBolunebilir) {
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
        } catch (HttpClientErrorException e) {
            List<List<ExpoPushMessage>> gruplar = projeyeGoreBolunebilir ? projeGruplari(e.getResponseBodyAsString(), batch) : List.of();
            if (gruplar.isEmpty()) {
                log.error("Failed to send push notifications batch", e);
                return;
            }
            log.warn("Push batch mixed {} Expo projects; resending per project", gruplar.size());
            for (List<ExpoPushMessage> grup : gruplar) {
                sendBatch(grup, false);
            }
        } catch (Exception e) {
            log.error("Failed to send push notifications batch", e);
        }
    }

    /**
     * 400 PUSH_TOO_MANY_EXPERIENCE_IDS gövdesinden proje gruplarını çıkarır:
     * {"errors":[{"code":"PUSH_TOO_MANY_EXPERIENCE_IDS","details":{"@sahip/proje":["ExponentPushToken[..]", ...]}}]}
     * Hata başka türdeyse ya da okunamazsa boş liste döner. Gövdede adı geçmeyen jetonlar ayrı bir grupta gönderilir.
     */
    List<List<ExpoPushMessage>> projeGruplari(String govde, List<ExpoPushMessage> batch) {
        List<List<ExpoPushMessage>> gruplar = new ArrayList<>();
        try {
            JsonNode errors = objectMapper.readTree(govde).path("errors");
            if (!errors.isArray()) return gruplar;
            for (JsonNode hata : errors) {
                if (!KARISIK_PROJE.equals(hata.path("code").asText())) continue;
                JsonNode details = hata.path("details");
                Set<String> atanan = new HashSet<>();
                Iterator<Map.Entry<String, JsonNode>> projeler = details.fields();
                while (projeler.hasNext()) {
                    Map.Entry<String, JsonNode> proje = projeler.next();
                    Set<String> jetonlar = new HashSet<>();
                    proje.getValue().forEach(j -> jetonlar.add(j.asText()));
                    List<ExpoPushMessage> grup = new ArrayList<>();
                    for (ExpoPushMessage m : batch) {
                        if (jetonlar.contains(m.getTo()) && atanan.add(m.getTo())) grup.add(m);
                    }
                    if (!grup.isEmpty()) gruplar.add(grup);
                }
                List<ExpoPushMessage> kalan = new ArrayList<>();
                for (ExpoPushMessage m : batch) {
                    if (!atanan.contains(m.getTo())) kalan.add(m);
                }
                if (!kalan.isEmpty()) gruplar.add(kalan);
                return gruplar;
            }
        } catch (Exception okunamadi) {
            log.warn("Could not parse Expo error body for project grouping", okunamadi);
        }
        return gruplar;
    }
}
