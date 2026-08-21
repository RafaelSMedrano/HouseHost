package com.househost.guest.application.service;

import com.househost.guest.application.dto.GuestRegisterRequestDTO;
import com.househost.guest.application.port.out.GuestPersistencePort;
import com.househost.shared.exception.GuestException;
import org.springframework.stereotype.Service;

@Service
class GuestValidationService {

    static final int MAX_CARE_TEXT_LENGTH = 4000;

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
        validateCareTextLength(
                request.preferencesAndRestrictions,
                "Preferencias e restricoes"
        );
        validateCareTextLength(
                request.accessibilityNeeds,
                "Necessidades de acessibilidade"
        );
    }

    private void validateCareTextLength(String careText, String fieldLabel) {
        if (careText != null && careText.length() > MAX_CARE_TEXT_LENGTH) {
            throw new GuestException(
                    fieldLabel + " deve ter no maximo " + MAX_CARE_TEXT_LENGTH + " caracteres."
            );
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
