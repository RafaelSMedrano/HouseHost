package com.househost.notifier.adapter.in.scheduling;

import com.househost.notifier.adapter.out.integration.Slf4jNotificationOperationalEventAdapter;
import com.househost.notifier.adapter.out.persistence.NotificationIntentPersistenceAdapter;
import com.househost.notifier.application.port.in.NotificationRequestUseCase;
import com.househost.notifier.application.port.out.EmailDeliveryPort;
import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.records.EmailDeliveryResultRecord;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.application.records.NotificationRequestRecord;
import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false",
                "spring.task.scheduling.pool.size=2",
                "househost.notifier.dispatch-enabled=true",
                "househost.notifier.initial-delay-ms=0",
                "househost.notifier.dispatch-delay-ms=5",
                "househost.notifier.batch-size=1",
                "househost.notifier.lease-duration=2s",
                "househost.notifier.maximum-attempts=3",
                "househost.notifier.retry-initial-delay=20ms",
                "househost.notifier.retry-maximum-delay=1s",
                "househost.notifier.retry-jitter-ratio=0"
        }
)
@Import({
        NotificationIntentPersistenceAdapter.class,
        Slf4jNotificationOperationalEventAdapter.class,
        NotifierApplicationConfiguration.class,
        NotificationDispatchScheduler.class,
        NotificationDispatchSchedulerIntegrationTest.DeliveryTestConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext
class NotificationDispatchSchedulerIntegrationTest {

    @Autowired
    private NotificationRequestUseCase notificationRequestUseCase;

    @Autowired
    private NotificationIntentPersistencePort notificationIntentPersistencePort;

    @Autowired
    private SchedulerRecordingEmailDeliveryAdapter schedulerRecordingEmailDeliveryAdapter;

    @Test
    void automaticallyDispatchesDueIntentsWithoutOverlappingFixedDelayCycles()
            throws Exception {
        UUID firstNotificationIntentId = notificationRequestUseCase.requestNotification(
                requestRecord("scheduler-event-1")
        );
        UUID secondNotificationIntentId = notificationRequestUseCase.requestNotification(
                requestRecord("scheduler-event-2")
        );

        assertTrue(schedulerRecordingEmailDeliveryAdapter.awaitDeliveries());
        awaitStatus(firstNotificationIntentId, NotificationStatus.ACCEPTED);
        awaitStatus(secondNotificationIntentId, NotificationStatus.ACCEPTED);

        assertEquals(1, schedulerRecordingEmailDeliveryAdapter.maximumConcurrency());
        assertEquals(2, schedulerRecordingEmailDeliveryAdapter.deliveryCount());
    }

    private void awaitStatus(
            UUID notificationIntentId,
            NotificationStatus expectedNotificationStatus
    ) throws InterruptedException {
        Instant deadline = Instant.now().plusSeconds(5);
        while (Instant.now().isBefore(deadline)) {
            NotificationStatus currentNotificationStatus =
                    notificationIntentPersistencePort
                            .findByIdOptional(notificationIntentId)
                            .orElseThrow()
                            .getStatus();
            if (currentNotificationStatus == expectedNotificationStatus) {
                return;
            }
            Thread.sleep(10);
        }
        assertEquals(
                expectedNotificationStatus,
                notificationIntentPersistencePort
                        .findByIdOptional(notificationIntentId)
                        .orElseThrow()
                        .getStatus()
        );
    }

    private NotificationRequestRecord requestRecord(String eventId) {
        return new NotificationRequestRecord(
                "HOUSEHOST",
                eventId,
                eventId + ":guest",
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
        );
    }

    @TestConfiguration
    static class DeliveryTestConfiguration {

        @Bean
        SchedulerRecordingEmailDeliveryAdapter schedulerRecordingEmailDeliveryAdapter() {
            return new SchedulerRecordingEmailDeliveryAdapter();
        }

        @Bean
        @Primary
        EmailDeliveryPort emailDeliveryPort(
                SchedulerRecordingEmailDeliveryAdapter
                        schedulerRecordingEmailDeliveryAdapter
        ) {
            return schedulerRecordingEmailDeliveryAdapter;
        }
    }

    static final class SchedulerRecordingEmailDeliveryAdapter implements EmailDeliveryPort {

        private final AtomicInteger activeDeliveryCount = new AtomicInteger();
        private final AtomicInteger maximumConcurrency = new AtomicInteger();
        private final AtomicInteger deliveryCount = new AtomicInteger();
        private final CountDownLatch deliveryLatch = new CountDownLatch(2);

        @Override
        public EmailDeliveryResultRecord deliver(
                String sourceSystem,
                String deliveryProfileKey,
                EmailMessageRecord emailMessageRecord
        ) {
            int currentActiveDeliveryCount = activeDeliveryCount.incrementAndGet();
            maximumConcurrency.accumulateAndGet(
                    currentActiveDeliveryCount,
                    Math::max
            );
            try {
                LockSupport.parkNanos(Duration.ofMillis(75).toNanos());
                int currentDeliveryCount = deliveryCount.incrementAndGet();
                return EmailDeliveryResultRecord.accepted(
                        "scheduler-provider-message-" + currentDeliveryCount
                );
            } finally {
                activeDeliveryCount.decrementAndGet();
                deliveryLatch.countDown();
            }
        }

        boolean awaitDeliveries() throws InterruptedException {
            return deliveryLatch.await(5, TimeUnit.SECONDS);
        }

        int maximumConcurrency() {
            return maximumConcurrency.get();
        }

        int deliveryCount() {
            return deliveryCount.get();
        }
    }
}
