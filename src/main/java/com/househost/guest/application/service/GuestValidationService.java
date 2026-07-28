package com.househost.guest.application.service;

import com.househost.guest.application.dto.GuestRegisterRequestDTO;
import com.househost.guest.application.port.out.GuestPersistencePort;
import com.househost.shared.exception.GuestException;
import org.springframework.stereotype.Service;

@Service
class GuestValidationService {

    private final GuestPersistencePort guestRepository;

    GuestValidationService(GuestPersistencePort guestRepository) {
        this.guestRepository = guestRepository;
    }

    void validateRegistration(GuestRegisterRequestDTO request) {
        validateRequest(request);
        validateUniqueEmail(request.email, null);
        validateUniqueDocumentNumber(request.documentNumber, null);
    }

    void validateUpdate(Long guestId, GuestRegisterRequestDTO request) {
        validateRequest(request);
        validateUniqueEmail(request.email, guestId);
        validateUniqueDocumentNumber(request.documentNumber, guestId);
    }

    private void validateRequest(GuestRegisterRequestDTO request) {
        if (request == null || request.fullName == null || request.fullName.isBlank()) {
            throw new GuestException("Nome completo e obrigatorio.");
        }
        if (request.rating != null && (request.rating < 0 || request.rating > 5)) {
            throw new GuestException("Avaliacao do hospede deve estar entre 0 e 5.");
        }
        if (request.stayCount != null && request.stayCount < 0) {
            throw new GuestException("Numero de estadias nao pode ser negativo.");
        }
        if (request.totalSpent != null && request.totalSpent.signum() < 0) {
            throw new GuestException("Total gasto nao pode ser negativo.");
        }
    }

    private void validateUniqueEmail(String email, Long guestId) {
        String normalizedEmail = normalizeOptional(email);
        boolean duplicated = normalizedEmail != null && (guestId == null
                ? guestRepository.existsByEmail(normalizedEmail)
                : guestRepository.existsByEmailAndIdNot(normalizedEmail, guestId));
        if (duplicated) {
            throw new GuestException("Email ja esta cadastrado para outro hospede.");
        }
    }

    private void validateUniqueDocumentNumber(String documentNumber, Long guestId) {
        String normalizedDocumentNumber = normalizeOptional(documentNumber);
        boolean duplicated = normalizedDocumentNumber != null && (guestId == null
                ? guestRepository.existsByDocumentNumber(normalizedDocumentNumber)
                : guestRepository.existsByDocumentNumberAndIdNot(normalizedDocumentNumber, guestId));
        if (duplicated) {
            throw new GuestException("Documento ja esta cadastrado para outro hospede.");
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
