package com.househost.stay.repository;

import com.househost.stay.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<CheckIn> findByStayId(Long stayId);

    Optional<CheckIn> findByBookingId(Long bookingId);

    boolean existsByStayId(Long stayId);

    boolean existsByBookingId(Long bookingId);
}
