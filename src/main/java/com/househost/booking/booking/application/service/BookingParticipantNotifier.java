package com.househost.booking.booking.application.service;

import com.househost.booking.booking.domain.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingParticipantNotifier {

    private final BookingGuestResolver bookingGuestResolver;

    public BookingParticipantNotifier(BookingGuestResolver bookingGuestResolver) {
        this.bookingGuestResolver = bookingGuestResolver;
    }

    public void notifyCreation(Booking booking) {
        bookingGuestResolver.resolveGuestStatus(booking.getGuest().getId());
    }

    public void notifyStatusChange(Booking booking) {
        bookingGuestResolver.resolveGuestStatus(booking.getGuest().getId());
    }

    public void notifyUpdate(Long previousGuestId, Booking booking) {
        Long currentGuestId = booking.getGuest().getId();
        if (!currentGuestId.equals(previousGuestId)) {
            bookingGuestResolver.resolveGuestStatus(previousGuestId);
        }
        bookingGuestResolver.resolveGuestStatus(currentGuestId);
    }

    public void notifyDeletion(Long guestId) {
        bookingGuestResolver.resolveGuestStatus(guestId);
    }

    public void notifyGuestStatusSynchronization(Long guestId) {
        bookingGuestResolver.resolveGuestStatus(guestId);
    }
}
