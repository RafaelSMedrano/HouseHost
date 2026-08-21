package com.househost.notifier.application.port.out;

import com.househost.notifier.application.records.NotificationClaimRecord;
import com.househost.notifier.domain.model.NotificationIntent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationIntentPersistencePort {

    NotificationIntent createIfAbsent(NotificationIntent notificationIntent);

    NotificationIntent save(NotificationIntent notificationIntent);

    Optional<NotificationIntent> findByIdOptional(UUID notificationIntentId);

    Optional<NotificationIntent> findBySourceSystemAndIdempotencyKeyOptional(
            String sourceSystem,
            String idempotencyKey
    );

    Optional<NotificationIntent> findByProviderMessageIdOptional(String providerMessageId);

    List<NotificationClaimRecord> claimEligibleNotificationClaimRecordList(
            Instant claimedAt,
            Instant leaseUntil,
            int batchSize
    );

    List<NotificationIntent> findRetentionExpiredNotificationIntentList(
            Instant referenceAt,
            int batchSize
    );

    int anonymizeContentByNotificationIntentIdList(
            List<UUID> notificationIntentIdList,
            Instant anonymizedAt
    );
}
