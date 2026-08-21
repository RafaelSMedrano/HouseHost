package com.househost.publicapi.adapter.out.integration;

import com.househost.booking.booking.adapter.out.persistence.BookingPersistenceAdapter;
import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.adapter.out.persistence.GuestPersistenceAdapter;
import com.househost.notifier.adapter.out.persistence.NotificationIntentPersistenceAdapter;
import com.househost.notifier.application.port.in.NotificationRequestUseCase;
import com.househost.notifier.application.port.out.EmailDeliveryPort;
import com.househost.notifier.application.port.out.NotificationOperationalEventPort;
import com.househost.notifier.application.records.EmailDeliveryResultRecord;
import com.househost.notifier.application.service.NotificationDispatchService;
import com.househost.notifier.application.service.NotificationIntentService;
import com.househost.notifier.application.service.NotificationRetryPolicy;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.privacy.policy.application.port.in.PublicPrivacyPolicyUseCase;
import com.househost.privacy.policy.application.records.PublishedPrivacyPolicyRecord;
import com.househost.publicapi.application.dto.PublicBookingRequestDTO;
import com.househost.publicapi.application.dto.PublicBookingResponseDTO;
import com.househost.publicapi.application.port.out.PublicBookingAuditPort;
import com.househost.publicapi.application.service.PublicBookingGuestResolver;
import com.househost.publicapi.application.service.PublicBookingNotificationResolver;
import com.househost.publicapi.application.service.PublicBookingParticipantNotifier;
import com.househost.publicapi.application.service.PublicBookingService;
import com.househost.room.adapter.out.persistence.RoomPersistenceAdapter;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:public-booking-notification;MODE=MySQL;DB_CLOSE_DELAY=-1"
})
@Import({
        BookingPersistenceAdapter.class,
        GuestPersistenceAdapter.class,
        RoomPersistenceAdapter.class,
        NotificationIntentPersistenceAdapter.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PublicBookingNotificationTransactionIntegrationTest {

    @Autowired
    private BookingPersistenceAdapter bookingPersistenceAdapter;

    @Autowired
    private GuestPersistenceAdapter guestPersistenceAdapter;

    @Autowired
    private RoomPersistenceAdapter roomPersistenceAdapter;

    @Autowired
    private NotificationIntentPersistenceAdapter notificationIntentPersistenceAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    private Room persistedRoom;
    private NotificationRequestUseCase notificationRequestUseCase;
    private Clock notificationClock;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from notification_provider_events");
        jdbcTemplate.update("delete from notification_intents");
        jdbcTemplate.update("delete from bookings");
        jdbcTemplate.update("delete from guests");
        jdbcTemplate.update("delete from rooms");
        persistedRoom = roomPersistenceAdapter.save(new Room(
                "Lavandas",
                RoomType.DOUBLE,
                4,
                new BigDecimal("350.00"),
                RoomStatus.AVAILABLE
        ));
        notificationClock = Clock.fixed(
                Instant.parse("2026-08-21T15:00:00Z"),
                java.time.ZoneOffset.UTC
        );
        notificationRequestUseCase = new NotificationIntentService(
                notificationIntentPersistenceAdapter,
                notificationClock,
                Duration.ofDays(30)
        );
    }

    @Test
    void commitsUnconfirmedBookingGuestEmailAndExactlyTwoIntents() {
        PublicBookingService publicBookingService = service(notificationRequestUseCase);

        PublicBookingResponseDTO publicBookingResponseDTO = transactionTemplate().execute(
                transactionStatus -> publicBookingService.createBooking(validRequest())
        );

        assertEquals(BookingStatus.UNCONFIRMED, publicBookingResponseDTO.status());
        assertEquals(1L, count("bookings"));
        assertEquals(1L, count("guests"));
        assertEquals(2L, count("notification_intents"));
        assertEquals(
                "maria.silva@example.com",
                jdbcTemplate.queryForObject("select email from guests", String.class)
        );
        assertEquals(
                2L,
                jdbcTemplate.queryForObject(
                        "select count(*) from notification_intents where external_event_id = ?",
                        Long.class,
                        "PUBLIC_BOOKING_REQUEST:" + publicBookingResponseDTO.bookingId()
                )
        );
        assertTrue(jdbcTemplate.queryForObject(
                "select text_body from notification_intents where notification_type = ?",
                String.class,
                "GUEST_REQUEST_RECEIVED"
        ).contains("ainda nao esta confirmada"));
    }

    @Test
    void rollsBackBookingGuestAndFirstIntentWhenSecondIntentFails() {
        AtomicInteger invocationCount = new AtomicInteger();
        NotificationRequestUseCase failingSecondNotificationRequestUseCase =
                notificationRequestRecord -> {
                    if (invocationCount.incrementAndGet() == 2) {
                        throw new IllegalStateException("Falha ao criar segunda intencao.");
                    }
                    return notificationRequestUseCase.requestNotification(
                            notificationRequestRecord
                    );
                };
        PublicBookingService publicBookingService = service(
                failingSecondNotificationRequestUseCase
        );

        assertThrows(
                IllegalStateException.class,
                () -> transactionTemplate().executeWithoutResult(
                        transactionStatus -> publicBookingService.createBooking(validRequest())
                )
        );

        assertEquals(0L, count("bookings"));
        assertEquals(0L, count("guests"));
        assertEquals(0L, count("notification_intents"));
    }

    @Test
    void rejectedRequestCreatesNoBookingGuestOrIntent() {
        PublicBookingService publicBookingService = service(notificationRequestUseCase);
        PublicBookingRequestDTO publicBookingRequestDTO = validRequest();
        publicBookingRequestDTO.guest.email = "email-invalido";

        assertThrows(
                RuntimeException.class,
                () -> transactionTemplate().executeWithoutResult(
                        transactionStatus -> publicBookingService.createBooking(
                                publicBookingRequestDTO
                        )
                )
        );

        assertEquals(0L, count("bookings"));
        assertEquals(0L, count("guests"));
        assertEquals(0L, count("notification_intents"));
    }

    @Test
    void repeatedHandlingOfSameEventKeepsOneIntentPerStableKey() {
        NotifierPublicBookingAdapter notifierPublicBookingAdapter = adapter(
                notificationRequestUseCase
        );
        com.househost.publicapi.application.records.PublicBookingNotificationRecord
                publicBookingNotificationRecord =
                new com.househost.publicapi.application.records.PublicBookingNotificationRecord(
                        "PUBLIC_BOOKING_REQUEST:42",
                        42L,
                        "CL-42",
                        LocalDateTime.of(2026, 8, 21, 12, 30),
                        "Lavandas",
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 13),
                        2,
                        1,
                        1,
                        new BigDecimal("1050.00"),
                        "BRL",
                        BookingStatus.UNCONFIRMED,
                        "Maria",
                        "Silva",
                        "maria.silva@example.com",
                        "+5512999999999"
                );

        notifierPublicBookingAdapter.requestNotifications(publicBookingNotificationRecord);
        notifierPublicBookingAdapter.requestNotifications(publicBookingNotificationRecord);

        assertEquals(2L, count("notification_intents"));
    }

    @Test
    void providerUnavailabilityChangesOnlyNotifierRetryState() {
        PublicBookingService publicBookingService = service(notificationRequestUseCase);
        PublicBookingResponseDTO publicBookingResponseDTO = transactionTemplate().execute(
                transactionStatus -> publicBookingService.createBooking(validRequest())
        );
        EmailDeliveryPort emailDeliveryPort = (
                sourceSystem,
                deliveryProfileKey,
                emailMessageRecord
        ) -> EmailDeliveryResultRecord.retryableFailure(
                NotificationFailureCategory.PROVIDER_UNAVAILABLE
        );
        NotificationDispatchService notificationDispatchService =
                new NotificationDispatchService(
                        notificationIntentPersistenceAdapter,
                        emailDeliveryPort,
                        mock(NotificationOperationalEventPort.class),
                        new NotificationRetryPolicy(
                                5,
                                Duration.ofSeconds(30),
                                Duration.ofMinutes(10),
                                0.0,
                                () -> 0.5
                        ),
                        Clock.offset(notificationClock, Duration.ofSeconds(1)),
                        Duration.ofMinutes(2),
                        10
                );

        notificationDispatchService.dispatchDueNotifications();

        assertEquals(
                BookingStatus.UNCONFIRMED.name(),
                jdbcTemplate.queryForObject(
                        "select status from bookings where id = ?",
                        String.class,
                        publicBookingResponseDTO.bookingId()
                )
        );
        assertEquals(
                2L,
                jdbcTemplate.queryForObject(
                        "select count(*) from notification_intents where status = ?",
                        Long.class,
                        "RETRYABLE_FAILURE"
                )
        );
    }

    private PublicBookingService service(
            NotificationRequestUseCase effectiveNotificationRequestUseCase
    ) {
        RoomService roomService = mock(RoomService.class);
        when(roomService.findRoomById(persistedRoom.getId())).thenReturn(persistedRoom);
        BookingService bookingService = mock(BookingService.class);
        PublicPrivacyPolicyUseCase publicPrivacyPolicyUseCase =
                mock(PublicPrivacyPolicyUseCase.class);
        when(publicPrivacyPolicyUseCase.requireCurrentPublishedForAcceptance(2L))
                .thenReturn(new PublishedPrivacyPolicyRecord(
                        2L,
                        2,
                        "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                        LocalDateTime.of(2026, 7, 26, 0, 0)
                ));
        PublicBookingAuditPort publicBookingAuditPort = mock(PublicBookingAuditPort.class);
        PublicBookingParticipantNotifier publicBookingParticipantNotifier =
                new PublicBookingParticipantNotifier(
                        new PublicBookingGuestResolver(
                                guestPersistenceAdapter,
                                bookingService
                        ),
                        new PublicBookingNotificationResolver(
                                adapter(effectiveNotificationRequestUseCase)
                        )
                );
        return new PublicBookingService(
                roomService,
                publicBookingParticipantNotifier,
                bookingPersistenceAdapter,
                publicBookingAuditPort,
                publicPrivacyPolicyUseCase
        );
    }

    private NotifierPublicBookingAdapter adapter(
            NotificationRequestUseCase effectiveNotificationRequestUseCase
    ) {
        PublicBookingNotificationProperties publicBookingNotificationProperties =
                new PublicBookingNotificationProperties();
        publicBookingNotificationProperties.setEnabled(true);
        publicBookingNotificationProperties.setManagementRecipient(
                "gerencia@example.com"
        );
        publicBookingNotificationProperties.validate();
        return new NotifierPublicBookingAdapter(
                effectiveNotificationRequestUseCase,
                publicBookingNotificationProperties
        );
    }

    private TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(platformTransactionManager);
    }

    private long count(String tableName) {
        return jdbcTemplate.queryForObject(
                "select count(*) from " + tableName,
                Long.class
        );
    }

    private PublicBookingRequestDTO validRequest() {
        PublicBookingRequestDTO publicBookingRequestDTO = new PublicBookingRequestDTO();
        publicBookingRequestDTO.roomId = persistedRoom.getId();
        publicBookingRequestDTO.checkIn = LocalDate.of(2026, 9, 10);
        publicBookingRequestDTO.checkOut = LocalDate.of(2026, 9, 13);
        publicBookingRequestDTO.adults = 2;
        publicBookingRequestDTO.children = 1;
        publicBookingRequestDTO.pets = 1;
        publicBookingRequestDTO.privacyPolicyId = 2L;
        publicBookingRequestDTO.termsVersion = "2026-06-04-public-pre-reserva";
        publicBookingRequestDTO.privacyAccepted = true;
        publicBookingRequestDTO.notes = "Precisamos de um berco.";
        publicBookingRequestDTO.guest = new PublicBookingRequestDTO.GuestData();
        publicBookingRequestDTO.guest.firstName = "Maria";
        publicBookingRequestDTO.guest.lastName = "Silva";
        publicBookingRequestDTO.guest.email = " MARIA.SILVA@EXAMPLE.COM ";
        publicBookingRequestDTO.guest.phone = "(12) 99999-9999";
        publicBookingRequestDTO.guest.city = "Sao Paulo - SP";
        return publicBookingRequestDTO;
    }
}
