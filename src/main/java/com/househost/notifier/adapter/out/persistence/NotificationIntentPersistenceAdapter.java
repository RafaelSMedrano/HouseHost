package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.adapter.out.persistence.entity.NotificationIntentJpaEntity;
import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.records.NotificationClaimRecord;
import com.househost.notifier.domain.model.NotificationIntent;
import com.househost.notifier.domain.model.NotificationStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationIntentPersistenceAdapter
        implements NotificationIntentPersistencePort {

    private static final List<NotificationStatus> READY_STATUS_LIST = List.of(
            NotificationStatus.PENDING,
            NotificationStatus.RETRYABLE_FAILURE
    );

    private final NotificationIntentJpaRepository notificationIntentJpaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public NotificationIntentPersistenceAdapter(
            NotificationIntentJpaRepository notificationIntentJpaRepository,
            JdbcTemplate jdbcTemplate,
            DataSource dataSource
    ) {
        this.notificationIntentJpaRepository = notificationIntentJpaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    @Transactional
    public NotificationIntent createIfAbsent(NotificationIntent notificationIntent) {
        String insertSql = """
                insert into notification_intents (
                    id,
                    source_system,
                    external_event_id,
                    idempotency_key,
                    correlation_key,
                    notification_type,
                    channel,
                    delivery_profile_key,
                    recipient,
                    subject,
                    text_body,
                    html_body,
                    status,
                    attempt_count,
                    next_attempt_at,
                    lease_until,
                    provider_message_id,
                    last_error_category,
                    created_at,
                    updated_at,
                    accepted_at,
                    delivered_at,
                    failed_at,
                    retention_until,
                    version
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """;
        if (isMysql()) {
            insertSql += " on duplicate key update id = id";
        }
        try {
            jdbcTemplate.update(
                    insertSql,
                    notificationIntent.getId().toString(),
                    notificationIntent.getSourceSystem(),
                    notificationIntent.getExternalEventId(),
                    notificationIntent.getIdempotencyKey(),
                    notificationIntent.getCorrelationKey(),
                    notificationIntent.getNotificationType(),
                    notificationIntent.getChannel().name(),
                    notificationIntent.getDeliveryProfileKey(),
                    notificationIntent.getRecipient(),
                    notificationIntent.getSubject(),
                    notificationIntent.getTextBody(),
                    notificationIntent.getHtmlBody(),
                    notificationIntent.getStatus().name(),
                    notificationIntent.getAttemptCount(),
                    toTimestamp(notificationIntent.getNextAttemptAt()),
                    toTimestamp(notificationIntent.getLeaseUntil()),
                    notificationIntent.getProviderMessageId(),
                    notificationIntent.getLastErrorCategory() == null
                            ? null
                            : notificationIntent.getLastErrorCategory().name(),
                    toTimestamp(notificationIntent.getCreatedAt()),
                    toTimestamp(notificationIntent.getUpdatedAt()),
                    toTimestamp(notificationIntent.getAcceptedAt()),
                    toTimestamp(notificationIntent.getDeliveredAt()),
                    toTimestamp(notificationIntent.getFailedAt()),
                    toTimestamp(notificationIntent.getRetentionUntil())
            );
        } catch (DuplicateKeyException duplicateKeyException) {
            if (isMysql()) {
                throw duplicateKeyException;
            }
        }
        return findBySourceSystemAndIdempotencyKeyOptional(
                notificationIntent.getSourceSystem(),
                notificationIntent.getIdempotencyKey()
        ).orElseThrow();
    }

    @Override
    @Transactional
    public NotificationIntent save(NotificationIntent notificationIntent) {
        NotificationIntentJpaEntity notificationIntentJpaEntity =
                NotificationIntentPersistenceMapper.toEntity(notificationIntent);
        return NotificationIntentPersistenceMapper.toDomain(
                notificationIntentJpaRepository.saveAndFlush(notificationIntentJpaEntity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationIntent> findByIdOptional(UUID notificationIntentId) {
        return notificationIntentJpaRepository.findById(notificationIntentId)
                .map(NotificationIntentPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationIntent> findBySourceSystemAndIdempotencyKeyOptional(
            String sourceSystem,
            String idempotencyKey
    ) {
        return notificationIntentJpaRepository
                .findBySourceSystemAndIdempotencyKey(sourceSystem, idempotencyKey)
                .map(NotificationIntentPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationIntent> findByProviderMessageIdOptional(
            String providerMessageId
    ) {
        return notificationIntentJpaRepository.findByProviderMessageId(providerMessageId)
                .map(NotificationIntentPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public List<NotificationClaimRecord> claimEligibleNotificationClaimRecordList(
            Instant claimedAt,
            Instant leaseUntil,
            int batchSize
    ) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Tamanho do lote deve ser positivo.");
        }
        List<NotificationIntentJpaEntity> notificationIntentJpaEntityList =
                notificationIntentJpaRepository.findEligibleForClaimList(
                        READY_STATUS_LIST,
                        NotificationStatus.PROCESSING,
                        claimedAt,
                        PageRequest.of(0, batchSize)
                );
        return notificationIntentJpaEntityList.stream()
                .map(notificationIntentJpaEntity -> claim(
                        notificationIntentJpaEntity,
                        claimedAt,
                        leaseUntil
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationIntent> findRetentionExpiredNotificationIntentList(
            Instant referenceAt,
            int batchSize
    ) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Tamanho do lote deve ser positivo.");
        }
        return notificationIntentJpaRepository.findRetentionExpiredList(
                        referenceAt,
                        PageRequest.of(0, batchSize)
                ).stream()
                .map(NotificationIntentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public int anonymizeContentByNotificationIntentIdList(
            List<UUID> notificationIntentIdList,
            Instant anonymizedAt
    ) {
        if (notificationIntentIdList.isEmpty()) {
            return 0;
        }
        return notificationIntentJpaRepository.anonymizeContentByIdList(
                notificationIntentIdList,
                anonymizedAt
        );
    }

    private NotificationClaimRecord claim(
            NotificationIntentJpaEntity notificationIntentJpaEntity,
            Instant claimedAt,
            Instant leaseUntil
    ) {
        NotificationIntent notificationIntent = NotificationIntentPersistenceMapper.toDomain(
                notificationIntentJpaEntity
        );
        notificationIntent.claim(claimedAt, leaseUntil);
        NotificationIntentPersistenceMapper.applyToEntity(
                notificationIntent,
                notificationIntentJpaEntity
        );
        return NotificationIntentPersistenceMapper.toClaimRecord(notificationIntent);
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
