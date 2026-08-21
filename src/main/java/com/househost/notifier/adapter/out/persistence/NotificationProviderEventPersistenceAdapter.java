package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.application.port.out.NotificationProviderEventPersistencePort;
import com.househost.notifier.domain.model.NotificationProviderEvent;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Component
public class NotificationProviderEventPersistenceAdapter
        implements NotificationProviderEventPersistencePort {

    private final NotificationProviderEventJpaRepository notificationProviderEventJpaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public NotificationProviderEventPersistenceAdapter(
            NotificationProviderEventJpaRepository notificationProviderEventJpaRepository,
            JdbcTemplate jdbcTemplate,
            DataSource dataSource
    ) {
        this.notificationProviderEventJpaRepository = notificationProviderEventJpaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    @Transactional
    public NotificationProviderEvent appendIfAbsent(
            NotificationProviderEvent notificationProviderEvent
    ) {
        String insertSql = """
                insert into notification_provider_events (
                    id,
                    notification_intent_id,
                    transport_event_id,
                    provider_event_id,
                    provider_message_id,
                    event_type,
                    bounce_type,
                    bounce_sub_type,
                    provider_status_code,
                    failure_category,
                    occurred_at,
                    received_at,
                    processed_at,
                    raw_event_storage_key
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        if (isMysql()) {
            insertSql += " on duplicate key update id = id";
        }
        try {
            jdbcTemplate.update(
                    insertSql,
                    notificationProviderEvent.getId().toString(),
                    notificationProviderEvent.getNotificationIntentId().toString(),
                    notificationProviderEvent.getTransportEventId(),
                    notificationProviderEvent.getProviderEventId(),
                    notificationProviderEvent.getProviderMessageId(),
                    notificationProviderEvent.getEventType().name(),
                    notificationProviderEvent.getBounceType(),
                    notificationProviderEvent.getBounceSubType(),
                    notificationProviderEvent.getProviderStatusCode(),
                    notificationProviderEvent.getFailureCategory() == null
                            ? null
                            : notificationProviderEvent.getFailureCategory().name(),
                    toTimestamp(notificationProviderEvent.getOccurredAt()),
                    toTimestamp(notificationProviderEvent.getReceivedAt()),
                    toTimestamp(notificationProviderEvent.getProcessedAt()),
                    notificationProviderEvent.getRawEventStorageKey()
            );
        } catch (DuplicateKeyException duplicateKeyException) {
            if (isMysql()) {
                throw duplicateKeyException;
            }
        }
        Optional<NotificationProviderEvent> persistedNotificationProviderEventOptional =
                notificationProviderEventJpaRepository.findByTransportEventId(
                                notificationProviderEvent.getTransportEventId()
                        )
                        .map(NotificationProviderEventPersistenceMapper::toDomain);
        if (persistedNotificationProviderEventOptional.isPresent()) {
            return persistedNotificationProviderEventOptional.orElseThrow();
        }
        return notificationProviderEventJpaRepository.findByProviderEventId(
                        notificationProviderEvent.getProviderEventId()
                )
                .map(NotificationProviderEventPersistenceMapper::toDomain)
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTransportEventId(String transportEventId) {
        return notificationProviderEventJpaRepository.existsByTransportEventId(
                transportEventId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProviderEventId(String providerEventId) {
        return notificationProviderEventJpaRepository.existsByProviderEventId(providerEventId);
    }

    private Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private boolean isMysql() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData()
                    .getDatabaseProductName()
                    .toLowerCase()
                    .contains("mysql");
        } catch (SQLException exception) {
            throw new IllegalStateException("Nao foi possivel identificar o banco.", exception);
        }
    }
}
