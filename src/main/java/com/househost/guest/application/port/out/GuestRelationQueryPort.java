package com.househost.guest.application.port.out;

import java.util.List;
import java.util.Map;

public interface GuestRelationQueryPort {
    List<Long> findBookingIds(Long guestId);
    Map<Long, List<Long>> findBookingIdsByGuestIds(List<Long> guestIds);
}
