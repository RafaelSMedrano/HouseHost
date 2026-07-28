package com.househost.booking.checkout.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import org.springframework.stereotype.Service;

@Service
public class CheckOutPartyResolverService {
    private final BookingService bookingService;
    private final GuestService guestService;
    private final RoomService roomService;

    public CheckOutPartyResolverService(BookingService bookingService,
                                        GuestService guestService, RoomService roomService) {
        this.bookingService = bookingService;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    Booking findBooking(Long bookingId) {
        return bookingService.findBooking(bookingId);
    }

    void resolveParties(CheckOut checkOut) {
        if (checkOut.getStatus() != CheckOutStatus.COMPLETED) {
            return;
        }
        resolveBooking(checkOut.getBooking());
        resolveGuest(checkOut.getGuest());
        resolveRoom(checkOut.getRoom(), checkOut.isRoomInspected());
    }

    private void resolveBooking(Booking booking) {
        if (booking != null) {
            bookingService.changeStatus(booking.getId(), BookingStatus.FINISHED);
        }
    }

    private void resolveGuest(Guest guest) {
        guestService.changeStatus(guest.getId(), GuestStatus.GOT_CHECKOUT);
    }

    private void resolveRoom(Room room, boolean roomInspected) {
        if (room == null || room.getStatus() == RoomStatus.INACTIVE) {
            return;
        }
        roomService.changeStatus(
                room.getId(),
                roomInspected ? RoomStatus.AVAILABLE : RoomStatus.MAINTENANCE
        );
    }
}
