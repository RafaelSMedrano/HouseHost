package com.househost.booking.checking.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import org.springframework.stereotype.Service;

@Service
public class CheckInBookingResolver {

    private final BookingService bookingService;

    public CheckInBookingResolver(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    void resolveBookingStatus(Booking booking) {
        bookingService.setStatus(booking.getId(), BookingStatus.IN_STAY);
    }
}
