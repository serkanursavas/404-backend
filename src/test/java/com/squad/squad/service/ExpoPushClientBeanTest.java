package com.squad.squad.service;

import com.squad.squad.repository.DeviceTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Spring'in ExpoPushClient'ı kurabildiğini doğrular (iki yapıcı varken @Autowired şart — staging bu yüzden açılmadı). */
class ExpoPushClientBeanTest {

    @Test
    void springBeaniKurabilir() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(DeviceTokenRepository.class, () -> mock(DeviceTokenRepository.class));
            ctx.register(ExpoPushClient.class);
            ctx.refresh();
            assertThat(ctx.getBean(ExpoPushClient.class)).isNotNull();
        }
    }
}
