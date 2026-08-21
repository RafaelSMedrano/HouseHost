package com.househost.ratings.application.port.out;

import com.househost.ratings.application.records.RatingPageRecord;
import com.househost.ratings.domain.model.Rating;

import java.util.Optional;

public interface RatingPersistencePort {

    Rating save(Rating rating);

    Optional<Rating> findById(Long id);

    Optional<Rating> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    RatingPageRecord findAll(int page, int size);

    RatingPageRecord findByGuestId(Long guestId, int page, int size);
}
