package com.househost.guest.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuestStatusTransitionTest {

    @Test
    void setStatusUsesInactiveWhenStatusIsNull() {
        Guest guest = new Guest();

        guest.setStatus(null);

        assertEquals(GuestStatus.INACTIVE, guest.getStatus());
    }

    @Test
    void setStatusAssignsRequestedStatus() {
        Guest guest = new Guest();

        guest.setStatus(GuestStatus.IN_STAY);

        assertEquals(GuestStatus.IN_STAY, guest.getStatus());
    }
}
