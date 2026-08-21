package com.househost.booking.checking.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckInParticipantResolverTest {

    @Test
    void bookingResolverStartsStayThroughBookingService() {
        BookingService bookingService = mock(BookingService.class);
        CheckInBookingResolver checkInBookingResolver = new CheckInBookingResolver(bookingService);
        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(23L);

        checkInBookingResolver.resolveBookingStatus(booking);

        verify(bookingService).setStatus(23L, BookingStatus.IN_STAY);
    }

    @Test
    void guestResolverStartsBookinglessStayThroughGuestService() {
        GuestService guestService = mock(GuestService.class);
        CheckInGuestResolver checkInGuestResolver = new CheckInGuestResolver(guestService);
        Guest guest = mock(Guest.class);
        when(guest.getId()).thenReturn(7L);

        checkInGuestResolver.resolveGuestStatus(guest);

        verify(guestService).setStatus(7L, GuestStatus.IN_STAY);
    }

    @Test
    void roomResolverOccupiesRoomThroughRoomService() {
        RoomService roomService = mock(RoomService.class);
        CheckInRoomResolver checkInRoomResolver = new CheckInRoomResolver(roomService);
        Room room = mock(Room.class);
        when(room.getId()).thenReturn(1L);

        checkInRoomResolver.resolveRoomStatus(room);

        verify(roomService).changeStatus(1L, RoomStatus.OCCUPIED);
    }
}
