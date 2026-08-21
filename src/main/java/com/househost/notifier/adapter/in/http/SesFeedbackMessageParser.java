package com.househost.notifier.adapter.in.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.notifier.application.records.NotificationFeedbackRecord;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationFailureCategory;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;

public class SesFeedbackMessageParser {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public SesFeedbackMessageParser(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public NotificationFeedbackRecord parse(
            String transportEventId,
            String sesMessage
    ) {
        try {
            JsonNode rootNode = objectMapper.readTree(sesMessage);
            NotificationEventType notificationEventType = eventType(rootNode);
            JsonNode detailNode = detailNode(rootNode, notificationEventType);
            Instant occurredAt = occurredAt(rootNode, detailNode);
            return new NotificationFeedbackRecord(
                    transportEventId,
                    textOrNull(detailNode, "feedbackId"),
                    requiredText(rootNode.path("mail"), "messageId"),
                    notificationEventType,
                    textOrNull(detailNode, "bounceType"),
                    textOrNull(detailNode, "bounceSubType"),
                    providerStatusCode(detailNode, notificationEventType),
                    failureCategory(detailNode, notificationEventType),
                    occurredAt,
                    clock.instant(),
                    null
            );
        } catch (SnsFeedbackException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw SnsFeedbackException.malformed(
                    "Evento SES malformado.",
                    exception
            );
        }
    }

    private NotificationEventType eventType(JsonNode rootNode) {
        String eventType = textOrNull(rootNode, "eventType");
        if (eventType == null) {
            eventType = textOrNull(rootNode, "notificationType");
        }
        if (eventType == null) {
            throw SnsFeedbackException.malformed("Tipo do evento SES ausente.", null);
        }
        return switch (eventType) {
            case "Delivery" -> NotificationEventType.DELIVERY;
            case "Bounce" -> NotificationEventType.BOUNCE;
            case "Complaint" -> NotificationEventType.COMPLAINT;
            case "Reject" -> NotificationEventType.REJECT;
            case "Rendering Failure" -> NotificationEventType.RENDERING_FAILURE;
            case "DeliveryDelay" -> NotificationEventType.DELIVERY_DELAY;
            default -> NotificationEventType.UNKNOWN;
        };
    }

    private JsonNode detailNode(
            JsonNode rootNode,
            NotificationEventType notificationEventType
    ) {
        return switch (notificationEventType) {
            case DELIVERY -> rootNode.path("delivery");
            case BOUNCE -> rootNode.path("bounce");
            case COMPLAINT -> rootNode.path("complaint");
            case REJECT -> rootNode.path("reject");
            case RENDERING_FAILURE -> rootNode.path("failure");
            case DELIVERY_DELAY -> rootNode.path("deliveryDelay");
            default -> rootNode.path("mail");
        };
    }

    private Instant occurredAt(JsonNode rootNode, JsonNode detailNode) {
        String timestamp = textOrNull(detailNode, "timestamp");
        if (timestamp == null) {
            timestamp = requiredText(rootNode.path("mail"), "timestamp");
        }
        return Instant.parse(timestamp);
    }

    private NotificationFailureCategory failureCategory(
            JsonNode detailNode,
            NotificationEventType notificationEventType
    ) {
        return switch (notificationEventType) {
            case BOUNCE -> "Permanent".equalsIgnoreCase(
                    textOrNull(detailNode, "bounceType")
            )
                    ? NotificationFailureCategory.PERMANENT_BOUNCE
                    : NotificationFailureCategory.TRANSIENT_BOUNCE;
            case COMPLAINT -> NotificationFailureCategory.COMPLAINT;
            case REJECT, RENDERING_FAILURE -> NotificationFailureCategory.CONTENT_REJECTED;
            case DELIVERY_DELAY -> NotificationFailureCategory.DELIVERY_DELAY;
            default -> null;
        };
    }

    private String providerStatusCode(
            JsonNode detailNode,
            NotificationEventType notificationEventType
    ) {
        String collectionName = switch (notificationEventType) {
            case BOUNCE -> "bouncedRecipients";
            case DELIVERY_DELAY -> "delayedRecipients";
            default -> null;
        };
        if (collectionName == null) {
            return null;
        }
        JsonNode recipientNodeList = detailNode.path(collectionName);
        if (!recipientNodeList.isArray() || recipientNodeList.isEmpty()) {
            return null;
        }
        return textOrNull(recipientNodeList.get(0), "status");
    }

    private String requiredText(JsonNode jsonNode, String fieldName) {
        String value = textOrNull(jsonNode, fieldName);
        if (value == null) {
            throw SnsFeedbackException.malformed(
                    "Campo obrigatorio ausente no evento SES.",
                    null
            );
        }
        return value;
    }

    private String textOrNull(JsonNode jsonNode, String fieldName) {
        JsonNode valueNode = jsonNode.path(fieldName);
        if (!valueNode.isTextual() || valueNode.asText().isBlank()) {
            return null;
        }
        return valueNode.asText();
    }
}
