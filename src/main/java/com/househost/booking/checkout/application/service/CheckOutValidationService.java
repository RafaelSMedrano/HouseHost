package com.househost.booking.checkout.application.service;

import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;

@Service
public class CheckOutValidationService {
    private final CheckOutPersistencePort checkOutRepository;

    public CheckOutValidationService(CheckOutPersistencePort checkOutRepository) {
        this.checkOutRepository = checkOutRepository;
    }

    void validateRequest(CheckOutRequestDTO request) {
        if (request == null) {
            throw new BookingException("Dados do check-out sao obrigatorios.");
        }
        if (request.bookingId == null) {
            throw new BookingException("Reserva e obrigatoria para o check-out.");
        }
        CheckOutStatus checkOutStatus = request.status == null
                ? CheckOutStatus.COMPLETED
                : request.status;
        if (checkOutStatus != CheckOutStatus.COMPLETED
                && request.paymentMaterialization != null) {
            throw new BookingException(
                    "Pagamento agendado somente pode ser materializado no checkout concluido."
            );
        }
        if (checkOutStatus == CheckOutStatus.COMPLETED) {
            validateCompletedRating(request.rating);
        }
    }

    void validateUnique(Long bookingId, Long currentId) {
        checkOutRepository.findByBookingId(bookingId).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BookingException("Reserva ja possui check-out.");
            }
        });
    }

    private void validateCompletedRating(CheckOutRatingRequestDTO checkOutRatingRequestDTO) {
        if (checkOutRatingRequestDTO == null) {
            throw new BookingException(
                    "A avaliacao completa e obrigatoria para concluir o check-out."
            );
        }
        validateScore(checkOutRatingRequestDTO.checkInProcedureScore);
        validateScore(checkOutRatingRequestDTO.checkOutProcedureScore);
        validateScore(checkOutRatingRequestDTO.accommodationCleanlinessScore);
        validateScore(checkOutRatingRequestDTO.teamCommunicationScore);
        validateScore(checkOutRatingRequestDTO.locationScore);
        validateScore(checkOutRatingRequestDTO.comfortScore);
    }

    private void validateScore(Integer score) {
        if (score == null || score < 1 || score > 5) {
            throw new BookingException(
                    "Todos os criterios da avaliacao devem ter nota inteira entre 1 e 5."
            );
        }
    }
}
