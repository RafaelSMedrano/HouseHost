package com.househost.booking.booking.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.domain.model.Guest;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingParticipantNotifierTest {

    @Test
    void bookingTransferRecomputesPreviousAndCurrentGuest() {
        BookingGuestResolver bookingGuestResolver = mock(BookingGuestResolver.class);
        BookingParticipantNotifier bookingParticipantNotifier =
                new BookingParticipantNotifier(bookingGuestResolver);
        Booking booking = bookingForGuest(9L);

        bookingParticipantNotifier.notifyUpdate(7L, booking);

        verify(bookingGuestResolver).resolveGuestStatus(7L);
        verify(bookingGuestResolver).resolveGuestStatus(9L);
    }

    @Test
    void bookingDeletionRecomputesAffectedGuest() {
        BookingGuestResolver bookingGuestResolver = mock(BookingGuestResolver.class);
        BookingParticipantNotifier bookingParticipantNotifier =
                new BookingParticipantNotifier(bookingGuestResolver);

        bookingParticipantNotifier.notifyDeletion(7L);

        verify(bookingGuestResolver).resolveGuestStatus(7L);
    }

    @Test
    void explicitSynchronizationRecomputesAffectedGuest() {
        BookingGuestResolver bookingGuestResolver = mock(BookingGuestResolver.class);
        BookingParticipantNotifier bookingParticipantNotifier =
                new BookingParticipantNotifier(bookingGuestResolver);

        bookingParticipantNotifier.notifyGuestStatusSynchronization(7L);

        verify(bookingGuestResolver).resolveGuestStatus(7L);
    }

    private Booking bookingForGuest(Long guestId) {
        Guest guest = mock(Guest.class);
        when(guest.getId()).thenReturn(guestId);
        Booking booking = mock(Booking.class);
        when(booking.getGuest()).thenReturn(guest);
        return booking;
    }
}
