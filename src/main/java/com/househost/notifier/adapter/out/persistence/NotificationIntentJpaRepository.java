package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.adapter.out.persistence.entity.NotificationIntentJpaEntity;
import com.househost.notifier.domain.model.NotificationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationIntentJpaRepository
        extends JpaRepository<NotificationIntentJpaEntity, UUID> {

    Optional<NotificationIntentJpaEntity> findBySourceSystemAndIdempotencyKey(
            String sourceSystem,
            String idempotencyKey
    );

    Optional<NotificationIntentJpaEntity> findByProviderMessageId(String providerMessageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select notificationIntentJpaEntity
            from NotificationIntentJpaEntity notificationIntentJpaEntity
            where notificationIntentJpaEntity.recipient is not null
              and (
                    (
                        notificationIntentJpaEntity.status in :readyStatusList
                        and notificationIntentJpaEntity.nextAttemptAt <= :referenceAt
                    )
                    or (
                        notificationIntentJpaEntity.status = :processingStatus
                        and notificationIntentJpaEntity.leaseUntil <= :referenceAt
                    )
              )
            order by case
                        when notificationIntentJpaEntity.status = :processingStatus
                            then notificationIntentJpaEntity.leaseUntil
                        else notificationIntentJpaEntity.nextAttemptAt
                     end,
                     notificationIntentJpaEntity.createdAt
            """)
    List<NotificationIntentJpaEntity> findEligibleForClaimList(
            @Param("readyStatusList") List<NotificationStatus> readyStatusList,
            @Param("processingStatus") NotificationStatus processingStatus,
            @Param("referenceAt") Instant referenceAt,
            Pageable pageable
    );

    @Query("""
            select notificationIntentJpaEntity
            from NotificationIntentJpaEntity notificationIntentJpaEntity
            where notificationIntentJpaEntity.retentionUntil <= :referenceAt
              and notificationIntentJpaEntity.recipient is not null
            order by notificationIntentJpaEntity.retentionUntil,
                     notificationIntentJpaEntity.createdAt
            """)
    List<NotificationIntentJpaEntity> findRetentionExpiredList(
            @Param("referenceAt") Instant referenceAt,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificationIntentJpaEntity notificationIntentJpaEntity
            set notificationIntentJpaEntity.recipient = null,
                notificationIntentJpaEntity.subject = null,
                notificationIntentJpaEntity.textBody = null,
                notificationIntentJpaEntity.htmlBody = null,
                notificationIntentJpaEntity.correlationKey = null,
                notificationIntentJpaEntity.updatedAt = :anonymizedAt,
                notificationIntentJpaEntity.version = notificationIntentJpaEntity.version + 1
            where notificationIntentJpaEntity.id in :notificationIntentIdList
            """)
    int anonymizeContentByIdList(
            @Param("notificationIntentIdList") List<UUID> notificationIntentIdList,
            @Param("anonymizedAt") Instant anonymizedAt
    );
}
