package com.househost.ratings.application.service;

import com.househost.ratings.application.dto.RatingPageResponseDTO;
import com.househost.ratings.application.dto.RatingRequestDTO;
import com.househost.ratings.application.dto.RatingResponseDTO;
import com.househost.ratings.application.port.in.RatingUseCase;
import com.househost.ratings.application.port.out.RatingAuditPort;
import com.househost.ratings.application.port.out.RatingPersistencePort;
import com.househost.ratings.application.records.RatingCreationContextRecord;
import com.househost.ratings.application.records.RatingPageRecord;
import com.househost.ratings.domain.exception.RatingConflictException;
import com.househost.ratings.domain.model.Rating;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class RatingService implements RatingUseCase {

    private final RatingPersistencePort ratingPersistencePort;
    private final RatingValidationService ratingValidationService;
    private final RatingAuditPort ratingAuditPort;

    public RatingService(
            RatingPersistencePort ratingPersistencePort,
            RatingValidationService ratingValidationService,
            RatingAuditPort ratingAuditPort
    ) {
        this.ratingPersistencePort = ratingPersistencePort;
        this.ratingValidationService = ratingValidationService;
        this.ratingAuditPort = ratingAuditPort;
    }

    @Override
    @Transactional
    public RatingResponseDTO createForCompletedBooking(RatingRequestDTO request) {
        RatingCreationContextRecord ratingCreationContextRecord =
                ratingValidationService.validateCreation(request);
        Rating rating = new Rating(
                ratingCreationContextRecord.booking(),
                request.checkInProcedureScore,
                request.checkOutProcedureScore,
                request.accommodationCleanlinessScore,
                request.teamCommunicationScore,
                request.locationScore,
                request.comfortScore,
                ratingCreationContextRecord.normalizedObservations(),
                ratingCreationContextRecord.evaluatedAt()
        );

        try {
            Rating savedRating = ratingPersistencePort.save(rating);
            ratingAuditPort.record(
                    "RATING_CREATED",
                    savedRating.getId(),
                    Map.of(
                            "bookingId", savedRating.getBooking().getId(),
                            "outcome", "CREATED"
                    )
            );
            return new RatingResponseDTO(savedRating);
        } catch (DataIntegrityViolationException exception) {
            throw new RatingConflictException(
                    "A reserva ja possui uma avaliacao.",
                    exception
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RatingPageResponseDTO findAll(int page, int size) {
        ratingValidationService.validatePagination(page, size);
        RatingPageRecord ratingPageRecord = ratingPersistencePort.findAll(page, size);
        ratingAuditPort.record(
                "RATING_LIST_VIEWED",
                null,
                Map.of(
                        "page", page,
                        "size", size,
                        "resultCount", ratingPageRecord.ratingSummaryRecordList().size(),
                        "outcome", "READ"
                )
        );
        return new RatingPageResponseDTO(ratingPageRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingPageResponseDTO findByGuestId(Long guestId, int page, int size) {
        ratingValidationService.validateGuestId(guestId);
        ratingValidationService.validatePagination(page, size);
        RatingPageRecord ratingPageRecord = ratingPersistencePort.findByGuestId(
                guestId,
                page,
                size
        );
        ratingAuditPort.record(
                "GUEST_RATING_HISTORY_VIEWED",
                null,
                Map.of(
                        "guestId", guestId,
                        "page", page,
                        "size", size,
                        "resultCount", ratingPageRecord.ratingSummaryRecordList().size(),
                        "outcome", "READ"
                )
        );
        return new RatingPageResponseDTO(ratingPageRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBookingId(Long bookingId) {
        ratingValidationService.validateBookingId(bookingId);
        return ratingPersistencePort.existsByBookingId(bookingId);
    }
}
