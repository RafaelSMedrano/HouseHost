package com.househost.notifier.application.port.in;

import com.househost.notifier.application.records.NotificationRequestRecord;

import java.util.UUID;

public interface NotificationRequestUseCase {

    UUID requestNotification(NotificationRequestRecord notificationRequestRecord);
}
