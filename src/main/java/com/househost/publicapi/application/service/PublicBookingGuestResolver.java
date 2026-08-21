package com.househost.publicapi.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.application.port.out.GuestPersistencePort;
import com.househost.guest.domain.model.Guest;
import com.househost.publicapi.application.dto.PublicBookingRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class PublicBookingGuestResolver {

    private final GuestPersistencePort guestRepository;
    private final BookingService bookingService;

    public PublicBookingGuestResolver(
            GuestPersistencePort guestRepository,
            BookingService bookingService
    ) {
        this.guestRepository = guestRepository;
        this.bookingService = bookingService;
    }

    public Guest resolveNewGuest(PublicBookingRequestDTO.GuestData guestData) {
        Guest guest = new Guest(
                guestData.firstName + " " + guestData.lastName,
                guestData.email,
                guestData.phone,
                null,
                guestData.city,
                null
        );
        return guestRepository.save(guest);
    }

    public void resolveGuestStatus(Booking booking) {
        bookingService.synchronizeGuestStatus(booking.getGuest().getId());
    }
}
