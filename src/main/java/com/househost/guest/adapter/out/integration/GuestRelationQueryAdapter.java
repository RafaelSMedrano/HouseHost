package com.househost.guest.adapter.out.integration;

import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.application.port.out.GuestRelationQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GuestRelationQueryAdapter implements GuestRelationQueryPort {
    private final BookingPersistencePort bookingRepository;

    public GuestRelationQueryAdapter(BookingPersistencePort bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Long> findBookingIds(Long guestId) {
        return bookingRepository.findByGuestId(guestId).stream().map(Booking::getId).toList();
    }

    @Override
    public Map<Long, List<Long>> findBookingIdsByGuestIds(List<Long> guestIds) {
        if (guestIds.isEmpty()) {
            return Map.of();
        }
        return bookingRepository.findByGuestIdIn(guestIds).stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getGuest().getId(),
                        Collectors.mapping(Booking::getId, Collectors.toList())
                ));
    }

}
