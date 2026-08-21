package com.househost.ratings.application.port.in;

import com.househost.ratings.application.dto.RatingPageResponseDTO;
import com.househost.ratings.application.dto.RatingRequestDTO;
import com.househost.ratings.application.dto.RatingResponseDTO;

public interface RatingUseCase {

    RatingResponseDTO createForCompletedBooking(RatingRequestDTO request);

    RatingPageResponseDTO findAll(int page, int size);

    RatingPageResponseDTO findByGuestId(Long guestId, int page, int size);

    boolean existsByBookingId(Long bookingId);
}
