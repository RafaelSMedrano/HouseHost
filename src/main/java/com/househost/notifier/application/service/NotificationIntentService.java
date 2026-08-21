package com.househost.notifier.application.service;

import com.househost.notifier.application.port.in.NotificationRequestUseCase;
import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.application.records.NotificationRequestRecord;
import com.househost.notifier.domain.exception.NotificationDomainException;
import com.househost.notifier.domain.model.NotificationIntent;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class NotificationIntentService implements NotificationRequestUseCase {

    private final NotificationIntentPersistencePort notificationIntentPersistencePort;
    private final Clock clock;
    private final Duration contentRetentionDuration;
    private final Supplier<UUID> notificationIntentIdSupplier;

    public NotificationIntentService(
            NotificationIntentPersistencePort notificationIntentPersistencePort,
            Clock clock,
            Duration contentRetentionDuration
    ) {
        this(
                notificationIntentPersistencePort,
                clock,
                contentRetentionDuration,
                UUID::randomUUID
        );
    }

    public NotificationIntentService(
            NotificationIntentPersistencePort notificationIntentPersistencePort,
            Clock clock,
            Duration contentRetentionDuration,
            Supplier<UUID> notificationIntentIdSupplier
    ) {
        this.notificationIntentPersistencePort = requireValue(
                notificationIntentPersistencePort,
                "Porta de persistencia de notificacao e obrigatoria."
        );
        this.clock = requireValue(clock, "Relogio da notificacao e obrigatorio.");
        this.contentRetentionDuration = requirePositiveDuration(contentRetentionDuration);
        this.notificationIntentIdSupplier = requireValue(
                notificationIntentIdSupplier,
                "Gerador de identificador da notificacao e obrigatorio."
        );
    }

    @Override
    public UUID requestNotification(NotificationRequestRecord notificationRequestRecord) {
        requireValue(notificationRequestRecord, "Requisicao de notificacao e obrigatoria.");
        Optional<NotificationIntent> existingNotificationIntentOptional =
                notificationIntentPersistencePort
                        .findBySourceSystemAndIdempotencyKeyOptional(
                                notificationRequestRecord.sourceSystem(),
                                notificationRequestRecord.idempotencyKey()
                        );
        if (existingNotificationIntentOptional.isPresent()) {
            return existingNotificationIntentOptional.orElseThrow().getId();
        }

        Instant createdAt = clock.instant();
        EmailMessageRecord emailMessageRecord = notificationRequestRecord.emailMessageRecord();
        NotificationIntent notificationIntent = NotificationIntent.create(
                notificationIntentIdSupplier.get(),
                notificationRequestRecord.sourceSystem(),
                notificationRequestRecord.externalEventId(),
                notificationRequestRecord.idempotencyKey(),
                notificationRequestRecord.correlationKey(),
                notificationRequestRecord.notificationType(),
                notificationRequestRecord.channel(),
                notificationRequestRecord.deliveryProfileKey(),
                emailMessageRecord.recipient(),
                emailMessageRecord.subject(),
                emailMessageRecord.textBody(),
                emailMessageRecord.htmlBody(),
                createdAt,
                createdAt.plus(contentRetentionDuration)
        );

        return notificationIntentPersistencePort.createIfAbsent(notificationIntent).getId();
    }

    private Duration requirePositiveDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new NotificationDomainException(
                    "Duracao de retencao deve ser positiva."
            );
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
