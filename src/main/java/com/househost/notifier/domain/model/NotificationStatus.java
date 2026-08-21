package com.househost.notifier.domain.model;

public enum NotificationStatus {
    PENDING,
    PROCESSING,
    RETRYABLE_FAILURE,
    EXHAUSTED,
    ACCEPTED,
    DELIVERED,
    BOUNCED,
    COMPLAINT;

    public boolean isTerminal() {
        return this == EXHAUSTED || this == BOUNCED || this == COMPLAINT;
    }
}
