package com.househost.stay.repository;

import com.househost.stay.model.Stay;
import com.househost.stay.model.StayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface StayRepository extends JpaRepository<Stay, Long> {

    long countByStatus(StayStatus status);

    List<Stay> findByGuestId(Long guestId);

    List<Stay> findByRoomId(Long roomId);

    List<Stay> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    boolean existsByBookingIdAndIdNot(Long bookingId, Long id);

    @Query("""
            select count(stay) > 0
            from Stay stay
            where stay.room.id = :roomId
              and stay.status in :statuses
              and stay.checkInDate < :expectedCheckOutDate
              and stay.expectedCheckOutDate > :checkInDate
            """)
    boolean existsOverlappingStay(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("expectedCheckOutDate") LocalDate expectedCheckOutDate,
            @Param("statuses") Collection<StayStatus> statuses
    );

    @Query("""
            select count(stay) > 0
            from Stay stay
            where stay.room.id = :roomId
              and stay.id <> :stayId
              and stay.status in :statuses
              and stay.checkInDate < :expectedCheckOutDate
              and stay.expectedCheckOutDate > :checkInDate
            """)
    boolean existsOverlappingStayIgnoringId(
            @Param("roomId") Long roomId,
            @Param("stayId") Long stayId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("expectedCheckOutDate") LocalDate expectedCheckOutDate,
            @Param("statuses") Collection<StayStatus> statuses
    );
}
