package com.househost.notifier.adapter.in.scheduling;

import com.househost.notifier.application.port.in.NotificationFeedbackUseCase;
import com.househost.notifier.application.port.out.NotificationFeedbackTransactionPort;
import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.port.out.NotificationOperationalEventPort;
import com.househost.notifier.application.port.out.NotificationProviderEventPersistencePort;
import com.househost.notifier.application.service.NotificationFeedbackService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class NotifierFeedbackConfiguration {

    @Bean
    public NotificationFeedbackUseCase notificationFeedbackUseCase(
            NotificationIntentPersistencePort notificationIntentPersistencePort,
            NotificationProviderEventPersistencePort
                    notificationProviderEventPersistencePort,
            NotificationFeedbackTransactionPort notificationFeedbackTransactionPort,
            NotificationOperationalEventPort notificationOperationalEventPort
    ) {
        return new NotificationFeedbackService(
                notificationIntentPersistencePort,
                notificationProviderEventPersistencePort,
                notificationFeedbackTransactionPort,
                notificationOperationalEventPort,
                Clock.systemUTC()
        );
    }
}
