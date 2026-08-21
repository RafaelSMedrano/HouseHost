package com.househost.notifier.adapter.in.scheduling;

import com.househost.notifier.adapter.out.persistence.NotificationIntentPersistenceAdapter;
import com.househost.notifier.application.port.in.NotificationRequestUseCase;
import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.application.records.NotificationRequestRecord;
import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false",
                "househost.notifier.dispatch-enabled=false"
        }
)
@Import({
        NotificationIntentPersistenceAdapter.class,
        NotifierApplicationConfiguration.class,
        NotificationDispatchScheduler.class
})
@DirtiesContext
class NotifierDisabledDispatchIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private NotificationRequestUseCase notificationRequestUseCase;

    @Autowired
    private NotificationIntentPersistencePort notificationIntentPersistencePort;

    @Test
    void preservesPendingIntentAndConsumerOperationWithoutDeliveryAdapter() {
        UUID notificationIntentId = notificationRequestUseCase.requestNotification(
                new NotificationRequestRecord(
                        "HOUSEHOST",
                        "disabled-event",
                        "disabled-event:guest",
                        "support-reference",
                        "GUEST_REQUEST_RECEIVED",
                        NotificationChannel.EMAIL,
                        "HOUSEHOST_TRANSACTIONAL",
                        new EmailMessageRecord(
                                "guest@example.com",
                                "Request received",
                                "We received your request.",
                                "<p>We received your request.</p>"
                        )
                )
        );

        assertTrue(applicationContext.getBeansOfType(
                NotificationDispatchScheduler.class
        ).isEmpty());
        assertEquals(
                NotificationStatus.PENDING,
                notificationIntentPersistencePort
                        .findByIdOptional(notificationIntentId)
                        .orElseThrow()
                        .getStatus()
        );
    }
}
