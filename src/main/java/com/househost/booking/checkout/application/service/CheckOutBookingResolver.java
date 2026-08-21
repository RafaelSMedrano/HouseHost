package com.househost.booking.checkout.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import org.springframework.stereotype.Service;

@Service
public class CheckOutBookingResolver {

    private final BookingService bookingService;

    public CheckOutBookingResolver(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    void resolveBookingStatus(Booking booking) {
        bookingService.setStatus(booking.getId(), BookingStatus.FINISHED);
    }
}
