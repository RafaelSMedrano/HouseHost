package com.househost.booking.checkout.application.service;

import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.ratings.application.dto.RatingRequestDTO;
import com.househost.ratings.application.port.in.RatingUseCase;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class CheckOutRatingResolver {

    private final RatingUseCase ratingUseCase;

    public CheckOutRatingResolver(@Lazy RatingUseCase ratingUseCase) {
        this.ratingUseCase = ratingUseCase;
    }

    void resolveRating(
            CheckOut checkOut,
            CheckOutRatingRequestDTO checkOutRatingRequestDTO
    ) {
        RatingRequestDTO ratingRequestDTO = new RatingRequestDTO();
        ratingRequestDTO.bookingId = checkOut.getBooking().getId();
        ratingRequestDTO.checkInProcedureScore =
                checkOutRatingRequestDTO.checkInProcedureScore;
        ratingRequestDTO.checkOutProcedureScore =
                checkOutRatingRequestDTO.checkOutProcedureScore;
        ratingRequestDTO.accommodationCleanlinessScore =
                checkOutRatingRequestDTO.accommodationCleanlinessScore;
        ratingRequestDTO.teamCommunicationScore =
                checkOutRatingRequestDTO.teamCommunicationScore;
        ratingRequestDTO.locationScore = checkOutRatingRequestDTO.locationScore;
        ratingRequestDTO.comfortScore = checkOutRatingRequestDTO.comfortScore;
        ratingRequestDTO.observations = checkOutRatingRequestDTO.observations;
        ratingUseCase.createForCompletedBooking(ratingRequestDTO);
    }
}
