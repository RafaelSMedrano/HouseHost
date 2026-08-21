package com.househost.notifier.application.port.out;

import com.househost.notifier.domain.model.NotificationProviderEvent;

public interface NotificationProviderEventPersistencePort {

    NotificationProviderEvent appendIfAbsent(
            NotificationProviderEvent notificationProviderEvent
    );

    boolean existsByTransportEventId(String transportEventId);

    boolean existsByProviderEventId(String providerEventId);
}
