package com.househost.notifier.application.port.in;

import java.util.UUID;

public interface NotificationDispatchUseCase {

    void dispatchDueNotifications();

    void reprocessExhaustedNotification(UUID notificationIntentId);
}
