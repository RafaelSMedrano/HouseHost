package com.househost.publicapi.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.domain.model.Guest;
import com.househost.publicapi.application.dto.PublicBookingRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class PublicBookingParticipantNotifier {

    private final PublicBookingGuestResolver publicBookingGuestResolver;
    private final PublicBookingNotificationResolver publicBookingNotificationResolver;

    public PublicBookingParticipantNotifier(
            PublicBookingGuestResolver publicBookingGuestResolver,
            PublicBookingNotificationResolver publicBookingNotificationResolver
    ) {
        this.publicBookingGuestResolver = publicBookingGuestResolver;
        this.publicBookingNotificationResolver = publicBookingNotificationResolver;
    }

    public Guest notifyGuestCreation(PublicBookingRequestDTO.GuestData guestData) {
        return publicBookingGuestResolver.resolveNewGuest(guestData);
    }

    public void notifyBookingCreation(Booking booking) {
        publicBookingGuestResolver.resolveGuestStatus(booking);
    }

    public void notifyReservationRequest(Booking booking) {
        publicBookingNotificationResolver.resolveReservationRequest(booking);
    }
}
