package com.househost.booking.booking.application.port.out;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingPersistencePort {

    Booking save(Booking booking);

    List<Booking> findAll();

    Optional<Booking> findById(Long id);

    void delete(Booking booking);

    long countByStatus(BookingStatus status);

    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByGuestIdIn(Collection<Long> guestIds);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findOverlappingBookings(
            Long roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Collection<BookingStatus> statuses
    );

    boolean existsOverlappingBooking(
            Long roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Collection<BookingStatus> statuses
    );

    boolean existsOverlappingBookingIgnoringId(
            Long roomId,
            Long bookingId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Collection<BookingStatus> statuses
    );
}
