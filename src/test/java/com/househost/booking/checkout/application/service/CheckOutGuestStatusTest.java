package com.househost.booking.checkout.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.room.domain.model.Room;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckOutGuestStatusTest {

    @Test
    void completionNotifiesBookingAndRoomResolvers() {
        CheckOutBookingResolver checkOutBookingResolver = mock(CheckOutBookingResolver.class);
        CheckOutRoomResolver checkOutRoomResolver = mock(CheckOutRoomResolver.class);
        CheckOutGuestResolver checkOutGuestResolver = mock(CheckOutGuestResolver.class);
        CheckOutRatingResolver checkOutRatingResolver = mock(CheckOutRatingResolver.class);
        CheckOutParticipantNotifier checkOutParticipantNotifier = new CheckOutParticipantNotifier(
                checkOutBookingResolver,
                checkOutRoomResolver,
                checkOutGuestResolver,
                checkOutRatingResolver,
                mock(CheckOutFinancialResolver.class)
        );
        CheckOut checkOut = mock(CheckOut.class);
        Booking booking = mock(Booking.class);
        Room room = mock(Room.class);
        when(checkOut.getStatus()).thenReturn(CheckOutStatus.COMPLETED);
        when(checkOut.getBooking()).thenReturn(booking);
        when(checkOut.getRoom()).thenReturn(room);
        when(checkOut.isRoomInspected()).thenReturn(true);
        when(checkOut.shouldApplyGuestHistory()).thenReturn(true);

        CheckOutRatingRequestDTO checkOutRatingRequestDTO = new CheckOutRatingRequestDTO();
        checkOutParticipantNotifier.notifyCompletion(checkOut, checkOutRatingRequestDTO);

        verify(checkOutBookingResolver).resolveBookingStatus(booking);
        verify(checkOutRoomResolver).resolveRoomStatus(room, true);
        verify(checkOutGuestResolver).resolveGuestHistory(checkOut);
        verify(checkOutRatingResolver).resolveRating(checkOut, checkOutRatingRequestDTO);
    }

    @Test
    void completedRetryDoesNotNotifyGuestHistoryAgain() {
        CheckOutBookingResolver checkOutBookingResolver = mock(CheckOutBookingResolver.class);
        CheckOutRoomResolver checkOutRoomResolver = mock(CheckOutRoomResolver.class);
        CheckOutGuestResolver checkOutGuestResolver = mock(CheckOutGuestResolver.class);
        CheckOutRatingResolver checkOutRatingResolver = mock(CheckOutRatingResolver.class);
        CheckOutParticipantNotifier checkOutParticipantNotifier = new CheckOutParticipantNotifier(
                checkOutBookingResolver,
                checkOutRoomResolver,
                checkOutGuestResolver,
                checkOutRatingResolver,
                mock(CheckOutFinancialResolver.class)
        );
        CheckOut checkOut = mock(CheckOut.class);
        when(checkOut.getStatus()).thenReturn(CheckOutStatus.COMPLETED);
        when(checkOut.shouldApplyGuestHistory()).thenReturn(false);

        CheckOutRatingRequestDTO checkOutRatingRequestDTO = new CheckOutRatingRequestDTO();
        checkOutParticipantNotifier.notifyCompletion(checkOut, checkOutRatingRequestDTO);

        verify(checkOutGuestResolver, never()).resolveGuestHistory(checkOut);
        verify(checkOutRatingResolver, never()).resolveRating(
                checkOut,
                checkOutRatingRequestDTO
        );
    }

    @Test
    void pendingAndCancelledCheckoutDoNotNotifyAnyParticipant() {
        CheckOutBookingResolver checkOutBookingResolver = mock(CheckOutBookingResolver.class);
        CheckOutRoomResolver checkOutRoomResolver = mock(CheckOutRoomResolver.class);
        CheckOutGuestResolver checkOutGuestResolver = mock(CheckOutGuestResolver.class);
        CheckOutRatingResolver checkOutRatingResolver = mock(CheckOutRatingResolver.class);
        CheckOutParticipantNotifier checkOutParticipantNotifier = new CheckOutParticipantNotifier(
                checkOutBookingResolver,
                checkOutRoomResolver,
                checkOutGuestResolver,
                checkOutRatingResolver,
                mock(CheckOutFinancialResolver.class)
        );
        CheckOut pendingCheckOut = mock(CheckOut.class);
        CheckOut cancelledCheckOut = mock(CheckOut.class);
        CheckOutRatingRequestDTO checkOutRatingRequestDTO = new CheckOutRatingRequestDTO();
        when(pendingCheckOut.getStatus()).thenReturn(CheckOutStatus.PENDING);
        when(cancelledCheckOut.getStatus()).thenReturn(CheckOutStatus.CANCELLED);

        checkOutParticipantNotifier.notifyCompletion(
                pendingCheckOut,
                checkOutRatingRequestDTO
        );
        checkOutParticipantNotifier.notifyCompletion(
                cancelledCheckOut,
                checkOutRatingRequestDTO
        );

        verify(checkOutBookingResolver, never()).resolveBookingStatus(any());
        verify(checkOutRoomResolver, never()).resolveRoomStatus(any(), anyBoolean());
        verify(checkOutGuestResolver, never()).resolveGuestHistory(any());
        verify(checkOutRatingResolver, never()).resolveRating(any(), any());
    }
}
