package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.port.out.BookingAuditPort;
import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.application.service.GuestService;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import com.househost.ratings.application.port.in.RatingUseCase;
import com.househost.ratings.domain.exception.RatingConflictException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceDeletionTest {

    @Test
    void deletesBookingAndRecordsSuccessAudit() {
        BookingPersistencePort persistencePort = mock(BookingPersistencePort.class);
        BookingAuditPort auditPort = mock(BookingAuditPort.class);
        Booking booking = booking();
        when(persistencePort.findById(booking.getId())).thenReturn(Optional.of(booking));
        BookingService bookingService = createBookingService(persistencePort, auditPort);

        bookingService.delete(booking.getId());

        verify(persistencePort).delete(booking);
        verify(auditPort).record(
                eq("BOOKING_DELETED"),
                eq("BOOKING"),
                eq(booking.getId()),
                anyMap()
        );
    }

    @Test
    void doesNotRecordSuccessAuditWhenPersistenceDeletionFails() {
        BookingPersistencePort persistencePort = mock(BookingPersistencePort.class);
        BookingAuditPort auditPort = mock(BookingAuditPort.class);
        Booking booking = booking();
        when(persistencePort.findById(booking.getId())).thenReturn(Optional.of(booking));
        doThrow(new IllegalStateException("database failure"))
                .when(persistencePort).delete(booking);
        BookingService bookingService = createBookingService(persistencePort, auditPort);

        assertThrows(IllegalStateException.class, () -> bookingService.delete(booking.getId()));

        verify(auditPort, never()).record(
                eq("BOOKING_DELETED"),
                eq("BOOKING"),
                eq(booking.getId()),
                anyMap()
        );
    }

    @Test
    void ratedBookingDeletionReturnsConflictWithoutDeletingOrAuditing() {
        BookingPersistencePort bookingPersistencePort = mock(BookingPersistencePort.class);
        BookingAuditPort bookingAuditPort = mock(BookingAuditPort.class);
        RatingUseCase ratingUseCase = mock(RatingUseCase.class);
        Booking booking = booking();
        when(bookingPersistencePort.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(ratingUseCase.existsByBookingId(booking.getId())).thenReturn(true);
        BookingService bookingService = createBookingService(
                bookingPersistencePort,
                bookingAuditPort,
                ratingUseCase
        );

        RatingConflictException ratingConflictException = assertThrows(
                RatingConflictException.class,
                () -> bookingService.delete(booking.getId())
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "A reserva possui avaliacao e nao pode ser removida antes do tratamento "
                        + "autorizado dessa avaliacao.",
                ratingConflictException.getMessage()
        );
        verify(bookingPersistencePort, never()).delete(booking);
        verify(bookingAuditPort, never()).record(
                eq("BOOKING_DELETED"),
                eq("BOOKING"),
                eq(booking.getId()),
                anyMap()
        );
    }

    @Test
    void missingBookingDoesNotQueryRatingsOrDeleteAnything() {
        BookingPersistencePort bookingPersistencePort = mock(BookingPersistencePort.class);
        BookingAuditPort bookingAuditPort = mock(BookingAuditPort.class);
        RatingUseCase ratingUseCase = mock(RatingUseCase.class);
        BookingService bookingService = createBookingService(
                bookingPersistencePort,
                bookingAuditPort,
                ratingUseCase
        );

        assertThrows(
                com.househost.shared.exception.BookingException.class,
                () -> bookingService.delete(999L)
        );

        verify(ratingUseCase, never()).existsByBookingId(999L);
        verify(bookingPersistencePort, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deletionIsTransactionalSoBookingAndHistoryDetachmentAreAtomic() throws Exception {
        Transactional transactional = BookingService.class
                .getMethod("delete", Long.class)
                .getAnnotation(Transactional.class);

        assertNotNull(transactional);
    }

    private BookingService createBookingService(
            BookingPersistencePort persistencePort,
            BookingAuditPort auditPort
    ) {
        return new BookingService(
                persistencePort,
                mock(GuestService.class),
                mock(RoomService.class),
                mock(BookingParticipantNotifier.class),
                auditPort,
                mock(BookingValidationService.class),
                mock(RatingUseCase.class)
        );
    }

    private BookingService createBookingService(
            BookingPersistencePort bookingPersistencePort,
            BookingAuditPort bookingAuditPort,
            RatingUseCase ratingUseCase
    ) {
        return new BookingService(
                bookingPersistencePort,
                mock(GuestService.class),
                mock(RoomService.class),
                mock(BookingParticipantNotifier.class),
                bookingAuditPort,
                mock(BookingValidationService.class),
                ratingUseCase
        );
    }

    private Booking booking() {
        Guest guest = new Guest("Roberto Jr", null, "+5512999999999", null, null, null);
        guest.restorePersistenceState(7L, null, List.of(), null, null);
        Room room = new Room(
                "101",
                RoomType.DOUBLE,
                2,
                new BigDecimal("350.00"),
                RoomStatus.AVAILABLE
        );
        room.restorePersistenceState(1L, null, null);
        Booking booking = new Booking(
                guest,
                room,
                LocalDate.of(2026, 8, 8),
                LocalDate.of(2026, 8, 10),
                BookingStatus.FINISHED,
                new BigDecimal("700.00")
        );
        booking.restorePersistenceState(
                23L,
                booking.getPaymentStatus(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null
        );
        return booking;
    }
}
