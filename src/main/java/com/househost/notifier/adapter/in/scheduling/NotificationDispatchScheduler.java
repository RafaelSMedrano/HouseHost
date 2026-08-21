package com.househost.notifier.adapter.in.scheduling;

import com.househost.notifier.application.port.in.NotificationDispatchUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "househost.notifier.dispatch-enabled",
        havingValue = "true"
)
public class NotificationDispatchScheduler {

    private final NotificationDispatchUseCase notificationDispatchUseCase;

    public NotificationDispatchScheduler(
            NotificationDispatchUseCase notificationDispatchUseCase
    ) {
        this.notificationDispatchUseCase = notificationDispatchUseCase;
    }

    @Scheduled(
            initialDelayString = "${househost.notifier.initial-delay-ms:15000}",
            fixedDelayString = "${househost.notifier.dispatch-delay-ms:10000}"
    )
    public void dispatchDueNotifications() {
        notificationDispatchUseCase.dispatchDueNotifications();
    }
}
