package com.househost.notifier.application.service;

import com.househost.notifier.application.port.in.NotificationDispatchUseCase;
import com.househost.notifier.application.port.out.EmailDeliveryPort;
import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.port.out.NotificationOperationalEventPort;
import com.househost.notifier.application.records.EmailDeliveryResultRecord;
import com.househost.notifier.application.records.NotificationClaimRecord;
import com.househost.notifier.application.records.NotificationRetryDecisionRecord;
import com.househost.notifier.domain.exception.NotificationDomainException;
import com.househost.notifier.domain.model.EmailDeliveryOutcome;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationIntent;
import com.househost.notifier.domain.model.NotificationStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class NotificationDispatchService implements NotificationDispatchUseCase {

    private final NotificationIntentPersistencePort notificationIntentPersistencePort;
    private final EmailDeliveryPort emailDeliveryPort;
    private final NotificationOperationalEventPort notificationOperationalEventPort;
    private final NotificationRetryPolicy notificationRetryPolicy;
    private final Clock clock;
    private final Duration leaseDuration;
    private final int batchSize;

    public NotificationDispatchService(
            NotificationIntentPersistencePort notificationIntentPersistencePort,
            EmailDeliveryPort emailDeliveryPort,
            NotificationOperationalEventPort notificationOperationalEventPort,
            NotificationRetryPolicy notificationRetryPolicy,
            Clock clock,
            Duration leaseDuration,
            int batchSize
    ) {
        this.notificationIntentPersistencePort = requireValue(
                notificationIntentPersistencePort,
                "Porta de persistencia de notificacao e obrigatoria."
        );
        this.emailDeliveryPort = requireValue(
                emailDeliveryPort,
                "Porta de entrega de email e obrigatoria."
        );
        this.notificationOperationalEventPort = requireValue(
                notificationOperationalEventPort,
                "Porta de evento operacional e obrigatoria."
        );
        this.notificationRetryPolicy = requireValue(
                notificationRetryPolicy,
                "Politica de retry e obrigatoria."
        );
        this.clock = requireValue(clock, "Relogio do dispatcher e obrigatorio.");
        this.leaseDuration = requirePositiveDuration(leaseDuration);
        if (batchSize <= 0) {
            throw new NotificationDomainException("Tamanho do lote deve ser positivo.");
        }
        this.batchSize = batchSize;
    }

    @Override
    public void dispatchDueNotifications() {
        Instant claimedAt = clock.instant();
        List<NotificationClaimRecord> notificationClaimRecordList =
                notificationIntentPersistencePort
                        .claimEligibleNotificationClaimRecordList(
                                claimedAt,
                                claimedAt.plus(leaseDuration),
                                batchSize
                        );
        safelyRecordBatchClaimed(notificationClaimRecordList.size(), claimedAt);
        for (NotificationClaimRecord notificationClaimRecord
                : notificationClaimRecordList) {
            try {
                dispatchClaim(notificationClaimRecord);
            } catch (RuntimeException exception) {
                safelyRecordDispatchInterrupted(notificationClaimRecord);
            }
        }
    }

    @Override
    public void reprocessExhaustedNotification(UUID notificationIntentId) {
        NotificationIntent notificationIntent = notificationIntentPersistencePort
                .findByIdOptional(requireValue(
                        notificationIntentId,
                        "Identificador da notificacao e obrigatorio."
                ))
                .orElseThrow(() -> new NotificationDomainException(
                        "Notificacao nao encontrada."
                ));
        notificationIntent.requeueExhausted(clock.instant());
        NotificationIntent requeuedNotificationIntent =
                notificationIntentPersistencePort.save(notificationIntent);
        safelyRecordRequeued(requeuedNotificationIntent);
    }

    private void dispatchClaim(NotificationClaimRecord notificationClaimRecord) {
        EmailDeliveryResultRecord emailDeliveryResultRecord = deliver(
                notificationClaimRecord
        );
        Instant outcomeRecordedAt = clock.instant();
        Optional<NotificationIntent> notificationIntentOptional =
                notificationIntentPersistencePort.findByIdOptional(
                        notificationClaimRecord.notificationIntentId()
                );
        if (notificationIntentOptional.isEmpty()) {
            safelyRecordClaimResultIgnored(notificationClaimRecord);
            return;
        }

        NotificationIntent notificationIntent = notificationIntentOptional.orElseThrow();
        if (!ownsCurrentClaim(notificationIntent, notificationClaimRecord)) {
            safelyRecordClaimResultIgnored(notificationClaimRecord);
            return;
        }

        applyOutcome(
                notificationIntent,
                emailDeliveryResultRecord,
                outcomeRecordedAt
        );
        NotificationIntent savedNotificationIntent =
                notificationIntentPersistencePort.save(notificationIntent);
        safelyRecordOutcome(savedNotificationIntent);
    }

    private EmailDeliveryResultRecord deliver(
            NotificationClaimRecord notificationClaimRecord
    ) {
        try {
            EmailDeliveryResultRecord emailDeliveryResultRecord = emailDeliveryPort.deliver(
                    notificationClaimRecord.sourceSystem(),
                    notificationClaimRecord.deliveryProfileKey(),
                    notificationClaimRecord.emailMessageRecord()
            );
            if (emailDeliveryResultRecord != null) {
                return emailDeliveryResultRecord;
            }
        } catch (RuntimeException exception) {
            return EmailDeliveryResultRecord.retryableFailure(
                    NotificationFailureCategory.UNKNOWN
            );
        }
        return EmailDeliveryResultRecord.retryableFailure(
                NotificationFailureCategory.UNKNOWN
        );
    }

    private boolean ownsCurrentClaim(
            NotificationIntent notificationIntent,
            NotificationClaimRecord notificationClaimRecord
    ) {
        return notificationIntent.getStatus() == NotificationStatus.PROCESSING
                && notificationIntent.getAttemptCount()
                == notificationClaimRecord.attemptCount()
                && Objects.equals(
                        notificationIntent.getLeaseUntil(),
                        notificationClaimRecord.leaseUntil()
                );
    }

    private void applyOutcome(
            NotificationIntent notificationIntent,
            EmailDeliveryResultRecord emailDeliveryResultRecord,
            Instant outcomeRecordedAt
    ) {
        if (emailDeliveryResultRecord.outcome() == EmailDeliveryOutcome.ACCEPTED) {
            notificationIntent.markAccepted(
                    emailDeliveryResultRecord.providerMessageId(),
                    outcomeRecordedAt
            );
            return;
        }
        if (emailDeliveryResultRecord.outcome() == EmailDeliveryOutcome.PERMANENT_FAILURE) {
            notificationIntent.markExhausted(
                    emailDeliveryResultRecord.failureCategory(),
                    outcomeRecordedAt
            );
            return;
        }

        NotificationRetryDecisionRecord notificationRetryDecisionRecord =
                notificationRetryPolicy.decide(
                        notificationIntent.getAttemptCount(),
                        outcomeRecordedAt,
                        emailDeliveryResultRecord.failureCategory()
                );
        if (notificationRetryDecisionRecord.targetStatus()
                == NotificationStatus.RETRYABLE_FAILURE) {
            notificationIntent.markRetryableFailure(
                    notificationRetryDecisionRecord.failureCategory(),
                    notificationRetryDecisionRecord.nextAttemptAt(),
                    outcomeRecordedAt
            );
            return;
        }
        notificationIntent.markExhausted(
                notificationRetryDecisionRecord.failureCategory(),
                outcomeRecordedAt
        );
    }

    private void safelyRecordBatchClaimed(int claimedCount, Instant claimedAt) {
        try {
            notificationOperationalEventPort.recordBatchClaimed(claimedCount, claimedAt);
        } catch (RuntimeException ignoredException) {
        }
    }

    private void safelyRecordOutcome(NotificationIntent notificationIntent) {
        try {
            notificationOperationalEventPort.recordOutcome(
                    notificationIntent.getId(),
                    notificationIntent.getSourceSystem(),
                    notificationIntent.getNotificationType(),
                    notificationIntent.getStatus(),
                    notificationIntent.getAttemptCount(),
                    notificationIntent.getLastErrorCategory(),
                    notificationIntent.getNextAttemptAt()
            );
        } catch (RuntimeException ignoredException) {
        }
    }

    private void safelyRecordClaimResultIgnored(
            NotificationClaimRecord notificationClaimRecord
    ) {
        try {
            notificationOperationalEventPort.recordClaimResultIgnored(
                    notificationClaimRecord.notificationIntentId(),
                    notificationClaimRecord.attemptCount()
            );
        } catch (RuntimeException ignoredException) {
        }
    }

    private void safelyRecordDispatchInterrupted(
            NotificationClaimRecord notificationClaimRecord
    ) {
        try {
            notificationOperationalEventPort.recordDispatchInterrupted(
                    notificationClaimRecord.notificationIntentId(),
                    notificationClaimRecord.attemptCount()
            );
        } catch (RuntimeException ignoredException) {
        }
    }

    private void safelyRecordRequeued(NotificationIntent notificationIntent) {
        try {
            notificationOperationalEventPort.recordRequeued(
                    notificationIntent.getId(),
                    notificationIntent.getSourceSystem(),
                    notificationIntent.getNotificationType()
            );
        } catch (RuntimeException ignoredException) {
        }
    }

    private Duration requirePositiveDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new NotificationDomainException("Duracao da concessao deve ser positiva.");
        }
        return duration;
    }

    private <T> T requireValue(T value, String message) {
        if (value == null) {
            throw new NotificationDomainException(message);
        }
        return value;
    }
}
