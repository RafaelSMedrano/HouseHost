package com.househost.publicapi.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.domain.model.Guest;
import com.househost.publicapi.application.port.out.PublicBookingNotificationPort;
import com.househost.publicapi.application.records.PublicBookingNotificationRecord;
import org.springframework.stereotype.Service;

@Service
public class PublicBookingNotificationResolver {

    private static final String EVENT_PREFIX = "PUBLIC_BOOKING_REQUEST:";

    private final PublicBookingNotificationPort publicBookingNotificationPort;

    public PublicBookingNotificationResolver(
            PublicBookingNotificationPort publicBookingNotificationPort
    ) {
        this.publicBookingNotificationPort = publicBookingNotificationPort;
    }

    public void resolveReservationRequest(Booking booking) {
        Guest guest = booking.getGuest();
        String[] guestNamePartArray = guest.getFullName().trim().split("\\s+", 2);
        String guestLastName = guestNamePartArray.length == 2
                ? guestNamePartArray[1]
                : "";
        String externalEventId = EVENT_PREFIX + booking.getId();
        PublicBookingNotificationRecord publicBookingNotificationRecord =
                new PublicBookingNotificationRecord(
                        externalEventId,
                        booking.getId(),
                        "CL-" + booking.getId(),
                        booking.getCreatedAt(),
                        booking.getRoom().getRoomNumber(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getAdults(),
                        booking.getChildren(),
                        booking.getPets(),
                        booking.getTotalAmount(),
                        "BRL",
                        booking.getStatus(),
                        guestNamePartArray[0],
                        guestLastName,
                        guest.getEmail(),
                        guest.getPhone()
                );
        publicBookingNotificationPort.requestNotifications(
                publicBookingNotificationRecord
        );
    }
}
