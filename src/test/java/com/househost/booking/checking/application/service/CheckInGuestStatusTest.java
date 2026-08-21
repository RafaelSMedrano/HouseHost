package com.househost.booking.checking.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckInGuestStatusTest {

    @Test
    void bookingBackedCompletionNotifiesBookingInsteadOfGuestDirectly() {
        CheckInBookingResolver checkInBookingResolver = mock(CheckInBookingResolver.class);
        CheckInGuestResolver checkInGuestResolver = mock(CheckInGuestResolver.class);
        CheckInRoomResolver checkInRoomResolver = mock(CheckInRoomResolver.class);
        CheckInParticipantNotifier checkInParticipantNotifier = new CheckInParticipantNotifier(
                checkInBookingResolver,
                checkInGuestResolver,
                checkInRoomResolver,
                mock(CheckInFinancialResolver.class)
        );
        CheckIn checkIn = completedCheckIn();
        Booking booking = mock(Booking.class);
        when(checkIn.getBooking()).thenReturn(booking);

        checkInParticipantNotifier.notifyCompletion(checkIn);

        verify(checkInBookingResolver).resolveBookingStatus(booking);
        verify(checkInGuestResolver, never()).resolveGuestStatus(checkIn.getGuest());
        verify(checkInRoomResolver).resolveRoomStatus(checkIn.getRoom());
    }

    @Test
    void bookinglessCompletionNotifiesGuestDirectly() {
        CheckInBookingResolver checkInBookingResolver = mock(CheckInBookingResolver.class);
        CheckInGuestResolver checkInGuestResolver = mock(CheckInGuestResolver.class);
        CheckInRoomResolver checkInRoomResolver = mock(CheckInRoomResolver.class);
        CheckInParticipantNotifier checkInParticipantNotifier = new CheckInParticipantNotifier(
                checkInBookingResolver,
                checkInGuestResolver,
                checkInRoomResolver,
                mock(CheckInFinancialResolver.class)
        );
        CheckIn checkIn = completedCheckIn();

        checkInParticipantNotifier.notifyCompletion(checkIn);

        verify(checkInGuestResolver).resolveGuestStatus(checkIn.getGuest());
        verify(checkInRoomResolver).resolveRoomStatus(checkIn.getRoom());
    }

    private CheckIn completedCheckIn() {
        CheckIn checkIn = mock(CheckIn.class);
        when(checkIn.getStatus()).thenReturn(CheckInStatus.COMPLETED);
        when(checkIn.getGuest()).thenReturn(mock(Guest.class));
        when(checkIn.getRoom()).thenReturn(mock(Room.class));
        return checkIn;
    }
}
