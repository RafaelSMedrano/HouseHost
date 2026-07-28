package com.househost.booking.checking.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;

@Service
public class CheckInPartyResolverService {
    private final BookingService bookingService;
    private final GuestService guestService;
    private final RoomService roomService;

    public CheckInPartyResolverService(BookingService bookingService, GuestService guestService,
                                       RoomService roomService) {
        this.bookingService = bookingService;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    Booking findBooking(Long bookingId) {
        if (bookingId == null) {
            return null;
        }
        return bookingService.findBooking(bookingId);
    }

    void resolveParties(CheckIn checkIn) {
        if (checkIn.getStatus() != CheckInStatus.COMPLETED) {
            return;
        }
        resolveBooking(checkIn.getBooking());
        resolveGuest(checkIn.getGuest());
        resolveRoom(checkIn.getRoom());
    }

    Guest resolveGuest(CheckInRequestDTO request, Booking booking) {
        if (booking != null) {
            return booking.getGuest();
        }
        if (request.guestId == null) {
            throw new BookingException("Hospede e obrigatorio.");
        }
        return guestService.findGuestById(request.guestId);
    }

    Room resolveRoom(CheckInRequestDTO request, Booking booking) {
        if (booking != null) {
            return booking.getRoom();
        }
        if (request.roomId == null) {
            throw new BookingException("Quarto e obrigatorio.");
        }
        return roomService.findRoomById(request.roomId);
    }

    private void resolveBooking(Booking booking) {
        if (booking != null) {
            bookingService.changeStatus(booking.getId(), BookingStatus.IN_STAY);
        }
    }

    private void resolveGuest(Guest guest) {
        guestService.changeStatus(guest.getId(), GuestStatus.IN_STAY);
    }

    private void resolveRoom(Room room) {
        roomService.changeStatus(room.getId(), RoomStatus.OCCUPIED);
    }
}
