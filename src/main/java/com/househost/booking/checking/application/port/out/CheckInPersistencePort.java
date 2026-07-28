package com.househost.booking.checking.application.port.out;

import com.househost.booking.checking.domain.model.CheckIn;
import java.util.List;
import java.util.Optional;

public interface CheckInPersistencePort {
    CheckIn save(CheckIn checkIn);
    List<CheckIn> findAll();
    Optional<CheckIn> findById(Long id);
    Optional<CheckIn> findByBookingId(Long bookingId);
    void delete(CheckIn checkIn);
}
