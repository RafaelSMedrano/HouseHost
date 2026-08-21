package com.househost.notifier.domain.model;

public enum NotificationFailureCategory {
    NETWORK,
    THROTTLED,
    PROVIDER_UNAVAILABLE,
    AUTHENTICATION,
    CONFIGURATION,
    INVALID_REQUEST,
    CONTENT_REJECTED,
    RECIPIENT_REJECTED,
    DELIVERY_DELAY,
    TRANSIENT_BOUNCE,
    PERMANENT_BOUNCE,
    COMPLAINT,
    UNKNOWN
}
