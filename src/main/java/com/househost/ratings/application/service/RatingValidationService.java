package com.househost.ratings.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checkout.application.service.CheckOutService;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.ratings.application.dto.RatingRequestDTO;
import com.househost.ratings.application.port.out.RatingPersistencePort;
import com.househost.ratings.application.records.RatingCreationContextRecord;
import com.househost.ratings.domain.exception.RatingConflictException;
import com.househost.ratings.domain.exception.RatingEligibilityException;
import com.househost.ratings.domain.exception.RatingException;
import com.househost.ratings.domain.model.Rating;
import org.springframework.stereotype.Service;

@Service
public class RatingValidationService {

    public static final int MAX_PAGE_SIZE = 100;

    private final BookingService bookingService;
    private final CheckOutService checkOutService;
    private final RatingPersistencePort ratingPersistencePort;

    public RatingValidationService(
            BookingService bookingService,
            CheckOutService checkOutService,
            RatingPersistencePort ratingPersistencePort
    ) {
        this.bookingService = bookingService;
        this.checkOutService = checkOutService;
        this.ratingPersistencePort = ratingPersistencePort;
    }

    public RatingCreationContextRecord validateCreation(RatingRequestDTO request) {
        if (request == null) {
            throw new RatingException("Os dados da avaliacao sao obrigatorios.");
        }
        if (request.bookingId == null) {
            throw new RatingException("A reserva avaliada e obrigatoria.");
        }

        validateScore(request.checkInProcedureScore, "procedimento de check-in");
        validateScore(request.checkOutProcedureScore, "procedimento de checkout");
        validateScore(request.accommodationCleanlinessScore, "limpeza das acomodacoes");
        validateScore(request.teamCommunicationScore, "comunicacao da equipe");
        validateScore(request.locationScore, "localizacao");
        validateScore(request.comfortScore, "conforto");
        String normalizedObservations = normalizeObservations(request.observations);

        Booking booking = bookingService.findBooking(request.bookingId);
        if (ratingPersistencePort.existsByBookingId(booking.getId())) {
            throw new RatingConflictException("A reserva ja possui uma avaliacao.");
        }

        CheckOut checkOut = checkOutService.findCheckOutByBookingId(booking.getId());
        if (checkOut == null
                || checkOut.getStatus() != CheckOutStatus.COMPLETED
                || checkOut.getActualCheckOutAt() == null) {
            throw new RatingEligibilityException(
                    "A avaliacao exige um checkout concluido para a reserva."
            );
        }

        return new RatingCreationContextRecord(
                booking,
                checkOut.getActualCheckOutAt(),
                normalizedObservations
        );
    }

    public void validatePagination(int page, int size) {
        if (page < 0) {
            throw new RatingException("A pagina da consulta nao pode ser negativa.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new RatingException("O tamanho da pagina deve estar entre 1 e 100.");
        }
    }

    public void validateGuestId(Long guestId) {
        if (guestId == null || guestId < 1) {
            throw new RatingException("Hospede invalido para o historico de avaliacoes.");
        }
    }

    public void validateBookingId(Long bookingId) {
        if (bookingId == null || bookingId < 1) {
            throw new RatingException("Reserva invalida para consulta de avaliacao.");
        }
    }

    private void validateScore(Integer score, String criterionName) {
        if (score == null || score < 1 || score > 5) {
            throw new RatingException(
                    "A nota de " + criterionName + " deve ser um inteiro entre 1 e 5."
            );
        }
    }

    private String normalizeObservations(String observations) {
        if (observations == null || observations.isBlank()) {
            return null;
        }

        String normalizedObservations = observations.trim();
        if (normalizedObservations.length() > Rating.MAX_OBSERVATIONS_LENGTH) {
            throw new RatingException(
                    "As observacoes da avaliacao devem ter no maximo 4000 caracteres."
            );
        }
        return normalizedObservations;
    }
}
