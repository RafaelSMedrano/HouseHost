package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingGuestResolver {

    private final BookingPersistencePort bookingRepository;
    private final GuestService guestService;

    public BookingGuestResolver(
            BookingPersistencePort bookingRepository,
            GuestService guestService
    ) {
        this.bookingRepository = bookingRepository;
        this.guestService = guestService;
    }

    public Guest resolveGuestStatus(Long guestId) {
        GuestStatus guestStatus = deriveGuestStatus(bookingRepository.findByGuestId(guestId));
        return guestService.setStatus(guestId, guestStatus);
    }

    private GuestStatus deriveGuestStatus(List<Booking> bookingList) {
        if (containsStatus(bookingList, BookingStatus.IN_STAY)) {
            return GuestStatus.IN_STAY;
        }
        if (containsStatus(bookingList, BookingStatus.CONFIRMED)) {
            return GuestStatus.WITH_CONFIRMED_BOOKING;
        }
        if (containsStatus(bookingList, BookingStatus.UNCONFIRMED)) {
            return GuestStatus.WITH_UNCONFIRMED_BOOKING;
        }
        return GuestStatus.INACTIVE;
    }

    private boolean containsStatus(List<Booking> bookingList, BookingStatus bookingStatus) {
        return bookingList.stream().anyMatch(booking -> booking.getStatus() == bookingStatus);
    }
}
