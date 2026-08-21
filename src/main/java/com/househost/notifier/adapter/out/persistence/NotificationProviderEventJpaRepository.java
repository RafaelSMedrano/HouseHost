package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.adapter.out.persistence.entity.NotificationProviderEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationProviderEventJpaRepository
        extends JpaRepository<NotificationProviderEventJpaEntity, UUID> {

    Optional<NotificationProviderEventJpaEntity> findByTransportEventId(
            String transportEventId
    );

    Optional<NotificationProviderEventJpaEntity> findByProviderEventId(
            String providerEventId
    );

    boolean existsByTransportEventId(String transportEventId);

    boolean existsByProviderEventId(String providerEventId);
}
