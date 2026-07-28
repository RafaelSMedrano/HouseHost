package com.househost.booking.checkout.application.port.out;

import com.househost.booking.checkout.domain.model.CheckOut;

import java.util.List;
import java.util.Optional;

public interface CheckOutPersistencePort {
    CheckOut save(CheckOut checkOut);
    List<CheckOut> findAll();
    Optional<CheckOut> findById(Long id);
    Optional<CheckOut> findByBookingId(Long bookingId);
    void delete(CheckOut checkOut);
}
