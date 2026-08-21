package com.househost.notifier.application.service;

import com.househost.notifier.application.records.NotificationRetryDecisionRecord;
import com.househost.notifier.domain.exception.NotificationDomainException;
import com.househost.notifier.domain.model.NotificationFailureCategory;

import java.time.Duration;
import java.time.Instant;
import java.util.function.DoubleSupplier;

public final class NotificationRetryPolicy {

    private final int maximumAttempts;
    private final Duration initialDelay;
    private final Duration maximumDelay;
    private final double jitterRatio;
    private final DoubleSupplier randomDoubleSupplier;

    public NotificationRetryPolicy(
            int maximumAttempts,
            Duration initialDelay,
            Duration maximumDelay,
            double jitterRatio,
            DoubleSupplier randomDoubleSupplier
    ) {
        if (maximumAttempts <= 0) {
            throw new NotificationDomainException(
                    "Quantidade maxima de tentativas deve ser positiva."
            );
        }
        this.maximumAttempts = maximumAttempts;
        this.initialDelay = requirePositiveDuration(initialDelay, "Atraso inicial");
        this.maximumDelay = requirePositiveDuration(maximumDelay, "Atraso maximo");
        if (maximumDelay.compareTo(initialDelay) < 0) {
            throw new NotificationDomainException(
                    "Atraso maximo nao pode ser menor que o atraso inicial."
            );
        }
        if (jitterRatio < 0.0 || jitterRatio > 1.0) {
            throw new NotificationDomainException(
                    "Proporcao de jitter deve estar entre zero e um."
            );
        }
        if (randomDoubleSupplier == null) {
            throw new NotificationDomainException("Gerador de jitter e obrigatorio.");
        }
        this.jitterRatio = jitterRatio;
        this.randomDoubleSupplier = randomDoubleSupplier;
    }

    public NotificationRetryDecisionRecord decide(
            int attemptCount,
            Instant failureRecordedAt,
            NotificationFailureCategory notificationFailureCategory
    ) {
        if (attemptCount <= 0) {
            throw new NotificationDomainException(
                    "Quantidade de tentativas deve ser positiva."
            );
        }
        if (failureRecordedAt == null) {
            throw new NotificationDomainException("Data da falha e obrigatoria.");
        }
        if (notificationFailureCategory == null) {
            throw new NotificationDomainException("Categoria da falha e obrigatoria.");
        }
        if (attemptCount >= maximumAttempts) {
            return NotificationRetryDecisionRecord.exhausted(
                    notificationFailureCategory
            );
        }

        long delayMillis = calculateDelayMillis(attemptCount);
        return NotificationRetryDecisionRecord.retryAt(
                failureRecordedAt.plusMillis(delayMillis),
                notificationFailureCategory
        );
    }

    private long calculateDelayMillis(int attemptCount) {
        long maximumDelayMillis = maximumDelay.toMillis();
        long initialDelayMillis = initialDelay.toMillis();
        int exponent = Math.min(attemptCount - 1, 62);
        long exponentialMultiplier = 1L << exponent;
        long exponentialDelayMillis = initialDelayMillis
                > maximumDelayMillis / exponentialMultiplier
                ? maximumDelayMillis
                : initialDelayMillis * exponentialMultiplier;
        double randomValue = randomDoubleSupplier.getAsDouble();
        if (randomValue < 0.0 || randomValue >= 1.0) {
            throw new NotificationDomainException(
                    "Gerador de jitter deve produzir valor entre zero inclusivo e um exclusivo."
            );
        }
        double signedRandomValue = randomValue * 2.0 - 1.0;
        double jitterMultiplier = 1.0 + signedRandomValue * jitterRatio;
        long jitteredDelayMillis = Math.max(
                1L,
                Math.round(exponentialDelayMillis * jitterMultiplier)
        );
        return Math.min(jitteredDelayMillis, maximumDelayMillis);
    }

    private Duration requirePositiveDuration(Duration duration, String fieldName) {
        if (duration == null
                || duration.isZero()
                || duration.isNegative()
                || duration.toMillis() == 0) {
            throw new NotificationDomainException(fieldName + " deve ser positivo.");
        }
        return duration;
    }
}
