package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.dto.BookingRequestDTO;
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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceGuestStatusTest {

    @Test
    void notifiesGuestAfterCreatingBooking() {
        BookingTestContextRecord bookingTestContextRecord = bookingTestContext();

        bookingTestContextRecord.bookingService.create(request());

        verify(bookingTestContextRecord.bookingParticipantNotifier).notifyCreation(
                bookingTestContextRecord.savedBooking
        );
    }

    @Test
    void notifiesGuestAfterSettingBookingStatus() {
        BookingTestContextRecord bookingTestContextRecord = bookingTestContext();
        when(bookingTestContextRecord.bookingPersistencePort.findById(23L))
                .thenReturn(Optional.of(bookingTestContextRecord.savedBooking));

        bookingTestContextRecord.bookingService.setStatus(23L, BookingStatus.CONFIRMED);

        verify(bookingTestContextRecord.bookingParticipantNotifier).notifyStatusChange(
                bookingTestContextRecord.savedBooking
        );
    }

    private BookingTestContextRecord bookingTestContext() {
        BookingPersistencePort bookingPersistencePort = mock(BookingPersistencePort.class);
        GuestService guestService = mock(GuestService.class);
        RoomService roomService = mock(RoomService.class);
        BookingParticipantNotifier bookingParticipantNotifier = mock(BookingParticipantNotifier.class);
        Guest guest = guest();
        Room room = room();
        Booking savedBooking = booking(guest, room);

        when(guestService.findGuestById(guest.getId())).thenReturn(guest);
        when(roomService.findRoomById(room.getId())).thenReturn(room);
        when(bookingPersistencePort.save(any(Booking.class))).thenReturn(savedBooking);
        BookingService bookingService = new BookingService(
                bookingPersistencePort,
                guestService,
                roomService,
                bookingParticipantNotifier,
                mock(BookingAuditPort.class),
                mock(BookingValidationService.class),
                mock(RatingUseCase.class)
        );
        return new BookingTestContextRecord(
                bookingService,
                bookingPersistencePort,
                bookingParticipantNotifier,
                savedBooking
        );
    }

    private BookingRequestDTO request() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.guestId = 7L;
        request.roomId = 1L;
        request.checkInDate = LocalDate.of(2026, 8, 20);
        request.checkOutDate = LocalDate.of(2026, 8, 22);
        request.status = BookingStatus.UNCONFIRMED;
        request.dailyRate = new BigDecimal("350.00");
        return request;
    }

    private Guest guest() {
        Guest guest = new Guest("Roberto Jr", null, "+5512999999999", null, null, null);
        guest.restorePersistenceState(7L, null, List.of(), null, null);
        return guest;
    }

    private Room room() {
        Room room = new Room(
                "101",
                RoomType.DOUBLE,
                2,
                new BigDecimal("350.00"),
                RoomStatus.AVAILABLE
        );
        room.restorePersistenceState(1L, null, null);
        return room;
    }

    private Booking booking(Guest guest, Room room) {
        Booking booking = new Booking(
                guest,
                room,
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                BookingStatus.UNCONFIRMED,
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

    private record BookingTestContextRecord(
            BookingService bookingService,
            BookingPersistencePort bookingPersistencePort,
            BookingParticipantNotifier bookingParticipantNotifier,
            Booking savedBooking
    ) {
    }
}
