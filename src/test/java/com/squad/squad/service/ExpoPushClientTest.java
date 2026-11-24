package com.squad.squad.service;

import com.squad.squad.dto.ExpoPushMessage;
import com.squad.squad.repository.DeviceTokenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpoPushClientTest {

    private static final String ESKI_1 = "ExponentPushToken[eski1]";
    private static final String ESKI_2 = "ExponentPushToken[eski2]";
    private static final String FUTZA = "ExponentPushToken[futza1]";

    private static ExpoPushMessage mesaj(String jeton) {
        return new ExpoPushMessage(jeton, "Baslik", "Govde", Map.of("type", "GAME_CREATED"));
    }

    private static HttpClientErrorException karisikProjeHatasi() {
        String govde = "{\"errors\":[{\"code\":\"PUSH_TOO_MANY_EXPERIENCE_IDS\",\"message\":\"All push notification messages"
                + " in the same request must be for the same project\",\"details\":{\"@serkanursavas/404-squad\":[\""
                + ESKI_1 + "\",\"" + ESKI_2 + "\"],\"@serkanursavas/futza\":[\"" + FUTZA + "\"]}}]}";
        return HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null,
                govde.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    @Test
    @SuppressWarnings("unchecked")
    void karisikProjelerProjeBasinaAyriGonderilir() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(karisikProjeHatasi())
                .thenReturn(Map.of("data", List.of()))
                .thenReturn(Map.of("data", List.of()));
        ExpoPushClient istemci = new ExpoPushClient(mock(DeviceTokenRepository.class), rest);

        istemci.sendPushNotifications(List.of(mesaj(ESKI_1), mesaj(FUTZA), mesaj(ESKI_2)));

        ArgumentCaptor<Object> govdeler = ArgumentCaptor.forClass(Object.class);
        verify(rest, times(3)).postForObject(anyString(), govdeler.capture(), eq(Map.class));
        List<Object> cagrilar = govdeler.getAllValues();
        assertThat(jetonlar(cagrilar.get(0))).containsExactly(ESKI_1, FUTZA, ESKI_2);
        assertThat(jetonlar(cagrilar.get(1))).containsExactly(ESKI_1, ESKI_2);
        assertThat(jetonlar(cagrilar.get(2))).containsExactly(FUTZA);
    }

    @Test
    void baskaHatadaTekrarDenenmez() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null,
                        "{\"errors\":[{\"code\":\"VALIDATION_ERROR\"}]}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        ExpoPushClient istemci = new ExpoPushClient(mock(DeviceTokenRepository.class), rest);

        istemci.sendPushNotifications(List.of(mesaj(ESKI_1), mesaj(FUTZA)));

        verify(rest, times(1)).postForObject(anyString(), any(), eq(Map.class));
    }

    @SuppressWarnings("unchecked")
    private static List<String> jetonlar(Object govde) {
        return ((List<ExpoPushMessage>) govde).stream().map(ExpoPushMessage::getTo).toList();
    }
}
