package com.househost.notifier.adapter.in.scheduling;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "househost.notifier")
public class NotifierDispatchProperties {

    private boolean dispatchEnabled;
    private long initialDelayMs = 15_000L;
    private long dispatchDelayMs = 10_000L;
    private int batchSize = 20;
    private Duration leaseDuration = Duration.ofMinutes(2);
    private int maximumAttempts = 5;
    private Duration retryInitialDelay = Duration.ofSeconds(30);
    private Duration retryMaximumDelay = Duration.ofMinutes(10);
    private double retryJitterRatio = 0.20;
    private Duration contentRetention = Duration.ofDays(30);

    @PostConstruct
    void validate() {
        if (initialDelayMs < 0) {
            throw invalidProperty("initial-delay-ms", "nao pode ser negativo");
        }
        if (dispatchDelayMs <= 0) {
            throw invalidProperty("dispatch-delay-ms", "deve ser positivo");
        }
        if (batchSize <= 0) {
            throw invalidProperty("batch-size", "deve ser positivo");
        }
        requirePositiveDuration(leaseDuration, "lease-duration");
        if (maximumAttempts <= 0) {
            throw invalidProperty("maximum-attempts", "deve ser positivo");
        }
        requirePositiveDuration(retryInitialDelay, "retry-initial-delay");
        requirePositiveDuration(retryMaximumDelay, "retry-maximum-delay");
        if (retryMaximumDelay.compareTo(retryInitialDelay) < 0) {
            throw invalidProperty(
                    "retry-maximum-delay",
                    "nao pode ser menor que retry-initial-delay"
            );
        }
        if (retryJitterRatio < 0.0 || retryJitterRatio > 1.0) {
            throw invalidProperty("retry-jitter-ratio", "deve estar entre zero e um");
        }
        requirePositiveDuration(contentRetention, "content-retention");
    }

    public boolean isDispatchEnabled() {
        return dispatchEnabled;
    }

    public void setDispatchEnabled(boolean dispatchEnabled) {
        this.dispatchEnabled = dispatchEnabled;
    }

    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    public void setInitialDelayMs(long initialDelayMs) {
        this.initialDelayMs = initialDelayMs;
    }

    public long getDispatchDelayMs() {
        return dispatchDelayMs;
    }

    public void setDispatchDelayMs(long dispatchDelayMs) {
        this.dispatchDelayMs = dispatchDelayMs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public Duration getLeaseDuration() {
        return leaseDuration;
    }

    public void setLeaseDuration(Duration leaseDuration) {
        this.leaseDuration = leaseDuration;
    }

    public int getMaximumAttempts() {
        return maximumAttempts;
    }

    public void setMaximumAttempts(int maximumAttempts) {
        this.maximumAttempts = maximumAttempts;
    }

    public Duration getRetryInitialDelay() {
        return retryInitialDelay;
    }

    public void setRetryInitialDelay(Duration retryInitialDelay) {
        this.retryInitialDelay = retryInitialDelay;
    }

    public Duration getRetryMaximumDelay() {
        return retryMaximumDelay;
    }

    public void setRetryMaximumDelay(Duration retryMaximumDelay) {
        this.retryMaximumDelay = retryMaximumDelay;
    }

    public double getRetryJitterRatio() {
        return retryJitterRatio;
    }

    public void setRetryJitterRatio(double retryJitterRatio) {
        this.retryJitterRatio = retryJitterRatio;
    }

    public Duration getContentRetention() {
        return contentRetention;
    }

    public void setContentRetention(Duration contentRetention) {
        this.contentRetention = contentRetention;
    }

    private void requirePositiveDuration(Duration duration, String propertyName) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw invalidProperty(propertyName, "deve ser positiva");
        }
    }

    private IllegalStateException invalidProperty(String propertyName, String message) {
        return new IllegalStateException(
                "househost.notifier." + propertyName + " " + message + "."
        );
    }
}
