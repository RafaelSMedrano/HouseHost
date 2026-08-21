package com.househost.booking.checking.application.service;

import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import org.springframework.stereotype.Service;

@Service
public class CheckInGuestResolver {

    private final GuestService guestService;

    public CheckInGuestResolver(GuestService guestService) {
        this.guestService = guestService;
    }

    void resolveGuestStatus(Guest guest) {
        guestService.setStatus(guest.getId(), GuestStatus.IN_STAY);
    }
}
