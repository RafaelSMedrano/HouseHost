package com.househost.booking.checkout.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.guest.application.service.GuestService;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;

@Service
public class CheckOutGuestResolver {

    private final GuestService guestService;

    public CheckOutGuestResolver(GuestService guestService) {
        this.guestService = guestService;
    }

    void resolveGuestHistory(CheckOut checkOut) {
        Booking booking = checkOut.getBooking();
        if (booking == null || checkOut.getActualCheckOutAt() == null) {
            throw new BookingException(
                    "Reserva e data efetiva sao obrigatorias para aplicar o historico do hospede."
            );
        }
        guestService.applyCompletedStay(
                checkOut.getGuest().getId(),
                checkOut.getActualCheckOutAt().toLocalDate(),
                booking.getTotalAmount()
        );
    }
}
