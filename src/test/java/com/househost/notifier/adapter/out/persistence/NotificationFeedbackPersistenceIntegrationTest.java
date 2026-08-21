package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.application.port.out.NotificationOperationalEventPort;
import com.househost.notifier.application.records.NotificationClaimRecord;
import com.househost.notifier.application.records.NotificationFeedbackRecord;
import com.househost.notifier.application.service.NotificationFeedbackService;
import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationIntent;
import com.househost.notifier.domain.model.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:notifier-feedback;MODE=MySQL;DB_CLOSE_DELAY=-1"
})
@Import({
        NotificationIntentPersistenceAdapter.class,
        NotificationProviderEventPersistenceAdapter.class,
        NotificationFeedbackTransactionAdapter.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationFeedbackPersistenceIntegrationTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-21T12:00:00Z");

    @Autowired
    private NotificationIntentPersistenceAdapter notificationIntentPersistenceAdapter;

    @Autowired
    private NotificationProviderEventPersistenceAdapter
            notificationProviderEventPersistenceAdapter;

    @Autowired
    private NotificationFeedbackTransactionAdapter notificationFeedbackTransactionAdapter;

    @Autowired
    private NotificationIntentJpaRepository notificationIntentJpaRepository;

    @Autowired
    private NotificationProviderEventJpaRepository notificationProviderEventJpaRepository;

    @MockBean
    private NotificationOperationalEventPort notificationOperationalEventPort;

    private NotificationFeedbackService notificationFeedbackService;

    @BeforeEach
    void setUp() {
        notificationProviderEventJpaRepository.deleteAll();
        notificationIntentJpaRepository.deleteAll();
        notificationFeedbackService = new NotificationFeedbackService(
                notificationIntentPersistenceAdapter,
                notificationProviderEventPersistenceAdapter,
                notificationFeedbackTransactionAdapter,
                notificationOperationalEventPort,
                Clock.fixed(CREATED_AT.plusSeconds(4), ZoneOffset.UTC)
        );
    }

    @Test
    void duplicateSnsDeliveryPersistsOneEventAndAppliesOneTransition() {
        NotificationIntent acceptedNotificationIntent = acceptedIntent();
        NotificationFeedbackRecord notificationFeedbackRecord = feedbackRecord(
                "sns-message-1",
                "ses-message-1"
        );

        notificationFeedbackService.processFeedback(notificationFeedbackRecord);
        notificationFeedbackService.processFeedback(notificationFeedbackRecord);

        NotificationIntent deliveredNotificationIntent =
                notificationIntentPersistenceAdapter.findByProviderMessageIdOptional(
                        "ses-message-1"
                ).orElseThrow();
        assertEquals(NotificationStatus.DELIVERED, deliveredNotificationIntent.getStatus());
        assertEquals(1, notificationProviderEventJpaRepository.count());
    }

    @Test
    void unknownProviderMessageIsAcknowledgedWithoutProviderEvent() {
        notificationFeedbackService.processFeedback(feedbackRecord(
                "sns-message-unknown",
                "ses-message-unknown"
        ));

        assertEquals(0, notificationProviderEventJpaRepository.count());
        assertEquals(0, notificationIntentJpaRepository.count());
    }

    private NotificationIntent acceptedIntent() {
        NotificationIntent notificationIntent = NotificationIntent.create(
                UUID.randomUUID(),
                "HOUSEHOST",
                "event-1",
                "event-1:guest",
                "support-reference-1",
                "GUEST_REQUEST_RECEIVED",
                NotificationChannel.EMAIL,
                "HOUSEHOST_TRANSACTIONAL",
                "guest@example.com",
                "Pedido recebido",
                "Recebemos seu pedido.",
                "<p>Recebemos seu pedido.</p>",
                CREATED_AT,
                CREATED_AT.plus(30, ChronoUnit.DAYS)
        );
        notificationIntentPersistenceAdapter.createIfAbsent(notificationIntent);
        List<NotificationClaimRecord> notificationClaimRecordList =
                notificationIntentPersistenceAdapter
                        .claimEligibleNotificationClaimRecordList(
                                CREATED_AT,
                                CREATED_AT.plusSeconds(30),
                                1
                        );
        NotificationIntent claimedNotificationIntent =
                notificationIntentPersistenceAdapter.findByIdOptional(
                        notificationClaimRecordList.getFirst().notificationIntentId()
                ).orElseThrow();
        claimedNotificationIntent.markAccepted("ses-message-1", CREATED_AT.plusSeconds(1));
        return notificationIntentPersistenceAdapter.save(claimedNotificationIntent);
    }

    private NotificationFeedbackRecord feedbackRecord(
            String transportEventId,
            String providerMessageId
    ) {
        return new NotificationFeedbackRecord(
                transportEventId,
                null,
                providerMessageId,
                NotificationEventType.DELIVERY,
                null,
                null,
                null,
                null,
                CREATED_AT.plusSeconds(2),
                CREATED_AT.plusSeconds(3),
                null
        );
    }
}
