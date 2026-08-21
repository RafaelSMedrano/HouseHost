package com.househost.notifier.domain.model;

public enum NotificationEventType {
    SEND,
    DELIVERY,
    BOUNCE,
    COMPLAINT,
    REJECT,
    RENDERING_FAILURE,
    DELIVERY_DELAY,
    UNKNOWN
}
