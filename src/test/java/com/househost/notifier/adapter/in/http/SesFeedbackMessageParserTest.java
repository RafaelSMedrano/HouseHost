package com.househost.notifier.adapter.in.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.notifier.application.records.NotificationFeedbackRecord;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SesFeedbackMessageParserTest {

    private static final Instant RECEIVED_AT = Instant.parse("2026-08-21T12:01:00Z");

    private final SesFeedbackMessageParser sesFeedbackMessageParser =
            new SesFeedbackMessageParser(
                    new ObjectMapper(),
                    Clock.fixed(RECEIVED_AT, ZoneOffset.UTC)
            );

    @Test
    void parsesPermanentBounceWithoutRecipientOrRawEnvelope() {
        NotificationFeedbackRecord notificationFeedbackRecord =
                sesFeedbackMessageParser.parse("sns-1", """
                        {
                          "eventType":"Bounce",
                          "mail":{
                            "messageId":"ses-1",
                            "timestamp":"2026-08-21T12:00:00Z",
                            "destination":["guest@example.com"]
                          },
                          "bounce":{
                            "feedbackId":"feedback-1",
                            "timestamp":"2026-08-21T12:00:05Z",
                            "bounceType":"Permanent",
                            "bounceSubType":"General",
                            "bouncedRecipients":[{
                              "emailAddress":"guest@example.com",
                              "status":"5.1.1",
                              "diagnosticCode":"smtp details"
                            }]
                          }
                        }
                        """);

        assertEquals(NotificationEventType.BOUNCE, notificationFeedbackRecord.eventType());
        assertEquals(
                NotificationFailureCategory.PERMANENT_BOUNCE,
                notificationFeedbackRecord.failureCategory()
        );
        assertEquals("5.1.1", notificationFeedbackRecord.providerStatusCode());
        assertEquals("feedback-1", notificationFeedbackRecord.providerEventId());
        assertNull(notificationFeedbackRecord.rawEventStorageKey());
    }

    @Test
    void normalizesAllSupportedNonBounceEvents() {
        Stream.of(
                eventCase("Delivery", "delivery", NotificationEventType.DELIVERY, null),
                eventCase(
                        "Complaint",
                        "complaint",
                        NotificationEventType.COMPLAINT,
                        NotificationFailureCategory.COMPLAINT
                ),
                eventCase(
                        "Reject",
                        "reject",
                        NotificationEventType.REJECT,
                        NotificationFailureCategory.CONTENT_REJECTED
                ),
                eventCase(
                        "Rendering Failure",
                        "failure",
                        NotificationEventType.RENDERING_FAILURE,
                        NotificationFailureCategory.CONTENT_REJECTED
                ),
                eventCase(
                        "DeliveryDelay",
                        "deliveryDelay",
                        NotificationEventType.DELIVERY_DELAY,
                        NotificationFailureCategory.DELIVERY_DELAY
                )
        ).forEach(this::assertEventCase);
    }

    @Test
    void rejectsMalformedNestedSesMessage() {
        assertThrows(
                SnsFeedbackException.class,
                () -> sesFeedbackMessageParser.parse(
                        "sns-1",
                        "{\"eventType\":\"Delivery\",\"mail\":{}}"
                )
        );
    }

    private EventCase eventCase(
            String providerEventType,
            String detailField,
            NotificationEventType notificationEventType,
            NotificationFailureCategory notificationFailureCategory
    ) {
        return new EventCase(
                providerEventType,
                detailField,
                notificationEventType,
                notificationFailureCategory
        );
    }

    private void assertEventCase(EventCase eventCaseRecord) {
        String message = """
                {
                  "notificationType":"%s",
                  "mail":{
                    "messageId":"ses-1",
                    "timestamp":"2026-08-21T12:00:00Z"
                  },
                  "%s":{"timestamp":"2026-08-21T12:00:05Z"}
                }
                """.formatted(
                eventCaseRecord.providerEventType(),
                eventCaseRecord.detailField()
        );

        NotificationFeedbackRecord notificationFeedbackRecord =
                sesFeedbackMessageParser.parse("sns-1", message);

        assertEquals(eventCaseRecord.notificationEventType(),
                notificationFeedbackRecord.eventType());
        assertEquals(eventCaseRecord.notificationFailureCategory(),
                notificationFeedbackRecord.failureCategory());
    }

    private record EventCase(
            String providerEventType,
            String detailField,
            NotificationEventType notificationEventType,
            NotificationFailureCategory notificationFailureCategory
    ) {
    }
}
