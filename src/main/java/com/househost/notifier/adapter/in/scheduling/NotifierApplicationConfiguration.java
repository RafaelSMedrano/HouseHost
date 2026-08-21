package com.househost.notifier.adapter.in.scheduling;

import com.househost.notifier.application.port.in.NotificationDispatchUseCase;
import com.househost.notifier.application.port.in.NotificationRequestUseCase;
import com.househost.notifier.application.port.out.EmailDeliveryPort;
import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.port.out.NotificationOperationalEventPort;
import com.househost.notifier.application.service.NotificationDispatchService;
import com.househost.notifier.application.service.NotificationIntentService;
import com.househost.notifier.application.service.NotificationRetryPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(NotifierDispatchProperties.class)
public class NotifierApplicationConfiguration {

    @Bean
    public NotificationRequestUseCase notificationRequestUseCase(
            NotificationIntentPersistencePort notificationIntentPersistencePort,
            NotifierDispatchProperties notifierDispatchProperties
    ) {
        return new NotificationIntentService(
                notificationIntentPersistencePort,
                Clock.systemUTC(),
                notifierDispatchProperties.getContentRetention()
        );
    }

    @Bean
    @ConditionalOnProperty(
            name = "househost.notifier.dispatch-enabled",
            havingValue = "true"
    )
    public NotificationDispatchUseCase notificationDispatchUseCase(
            NotificationIntentPersistencePort notificationIntentPersistencePort,
            EmailDeliveryPort emailDeliveryPort,
            NotificationOperationalEventPort notificationOperationalEventPort,
            NotifierDispatchProperties notifierDispatchProperties
    ) {
        NotificationRetryPolicy notificationRetryPolicy = new NotificationRetryPolicy(
                notifierDispatchProperties.getMaximumAttempts(),
                notifierDispatchProperties.getRetryInitialDelay(),
                notifierDispatchProperties.getRetryMaximumDelay(),
                notifierDispatchProperties.getRetryJitterRatio(),
                () -> ThreadLocalRandom.current().nextDouble()
        );
        return new NotificationDispatchService(
                notificationIntentPersistencePort,
                emailDeliveryPort,
                notificationOperationalEventPort,
                notificationRetryPolicy,
                Clock.systemUTC(),
                notifierDispatchProperties.getLeaseDuration(),
                notifierDispatchProperties.getBatchSize()
        );
    }
}
