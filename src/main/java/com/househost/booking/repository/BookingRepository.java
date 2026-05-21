package com.househost.booking.repository;

import com.househost.booking.model.Booking;
import com.househost.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByStatus(BookingStatus status);

    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByRoomId(Long roomId);

    @Query("""
            select count(booking) > 0
            from Booking booking
            where booking.room.id = :roomId
              and booking.status in :statuses
              and booking.checkInDate < :checkOutDate
              and booking.checkOutDate > :checkInDate
            """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statuses") Collection<BookingStatus> statuses
    );

    @Query("""
            select count(booking) > 0
            from Booking booking
            where booking.room.id = :roomId
              and booking.id <> :bookingId
              and booking.status in :statuses
              and booking.checkInDate < :checkOutDate
              and booking.checkOutDate > :checkInDate
            """)
    boolean existsOverlappingBookingIgnoringId(
            @Param("roomId") Long roomId,
            @Param("bookingId") Long bookingId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statuses") Collection<BookingStatus> statuses
    );
}
