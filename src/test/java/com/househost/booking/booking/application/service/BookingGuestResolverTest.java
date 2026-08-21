package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.GuestStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingGuestResolverTest {

    @Test
    void inStayOutranksEveryOtherBookingStatus() {
        assertResolvedStatus(
                List.of(
                        booking(BookingStatus.UNCONFIRMED),
                        booking(BookingStatus.CONFIRMED),
                        booking(BookingStatus.IN_STAY)
                ),
                GuestStatus.IN_STAY
        );
    }

    @Test
    void confirmedOutranksUnconfirmedBooking() {
        assertResolvedStatus(
                List.of(
                        booking(BookingStatus.UNCONFIRMED),
                        booking(BookingStatus.CONFIRMED)
                ),
                GuestStatus.WITH_CONFIRMED_BOOKING
        );
    }

    @Test
    void unconfirmedIsUsedWhenItIsTheOnlyActiveBooking() {
        assertResolvedStatus(
                List.of(
                        booking(BookingStatus.FINISHED),
                        booking(BookingStatus.UNCONFIRMED)
                ),
                GuestStatus.WITH_UNCONFIRMED_BOOKING
        );
    }

    @Test
    void inactiveIsUsedWhenThereIsNoActiveBooking() {
        assertResolvedStatus(
                List.of(
                        booking(BookingStatus.FINISHED),
                        booking(BookingStatus.CANCELED)
                ),
                GuestStatus.INACTIVE
        );
    }

    private void assertResolvedStatus(
            List<Booking> bookingList,
            GuestStatus expectedGuestStatus
    ) {
        BookingPersistencePort bookingPersistencePort = mock(BookingPersistencePort.class);
        GuestService guestService = mock(GuestService.class);
        BookingGuestResolver bookingGuestResolver = new BookingGuestResolver(
                bookingPersistencePort,
                guestService
        );
        when(bookingPersistencePort.findByGuestId(7L)).thenReturn(bookingList);

        bookingGuestResolver.resolveGuestStatus(7L);

        verify(guestService).setStatus(7L, expectedGuestStatus);
    }

    private Booking booking(BookingStatus bookingStatus) {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(bookingStatus);
        return booking;
    }
}
