package com.househost.notifier.domain.model;

public enum EmailDeliveryOutcome {
    ACCEPTED,
    RETRYABLE_FAILURE,
    PERMANENT_FAILURE
}
