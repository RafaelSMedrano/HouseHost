package com.househost.guest.service;

import com.househost.guest.dto.GuestRequestDTO;
import com.househost.guest.dto.GuestResponseDTO;
import com.househost.guest.model.Guest;
import com.househost.guest.repository.GuestRepository;
import com.househost.shared.dto.ResponseDTO;
import com.househost.shared.exception.GuestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public ResponseDTO create(GuestRequestDTO request) {
        validateRequest(request);
        validateUniqueEmail(request.email);
        validateUniqueDocumentNumber(request.documentNumber);

        Guest guest = new Guest(
                normalizeRequired(request.fullName),
                normalizeOptional(request.email),
                normalizeOptional(request.phone),
                normalizeOptional(request.documentNumber)
        );

        Guest savedGuest = guestRepository.save(guest);
        return new ResponseDTO("success", "Hospede cadastrado com sucesso", new GuestResponseDTO(savedGuest));
    }

    public ResponseDTO findAll() {
        List<GuestResponseDTO> guests = guestRepository.findAll()
                .stream()
                .map(GuestResponseDTO::new)
                .toList();

        return new ResponseDTO("success", "Hospedes encontrados com sucesso", guests);
    }

    public ResponseDTO findById(Long id) {
        Guest guest = findGuestById(id);
        return new ResponseDTO("success", "Hospede encontrado com sucesso", new GuestResponseDTO(guest));
    }

    public ResponseDTO update(Long id, GuestRequestDTO request) {
        validateRequest(request);

        Guest guest = findGuestById(id);
        validateUniqueEmail(request.email, id);
        validateUniqueDocumentNumber(request.documentNumber, id);

        guest.updateProfile(
                normalizeRequired(request.fullName),
                normalizeOptional(request.email),
                normalizeOptional(request.phone),
                normalizeOptional(request.documentNumber)
        );

        Guest savedGuest = guestRepository.save(guest);
        return new ResponseDTO("success", "Hospede atualizado com sucesso", new GuestResponseDTO(savedGuest));
    }

    public ResponseDTO delete(Long id) {
        Guest guest = findGuestById(id);
        guestRepository.delete(guest);
        return new ResponseDTO("success", "Hospede removido com sucesso", null);
    }

    private Guest findGuestById(Long id) {
        if (id == null) {
            throw new GuestException("Hospede nao encontrado.");
        }

        return guestRepository.findById(id)
                .orElseThrow(() -> new GuestException("Hospede nao encontrado."));
    }

    private void validateRequest(GuestRequestDTO request) {
        if (request == null || isBlank(request.fullName)) {
            throw new GuestException("Nome completo e obrigatorio.");
        }
    }

    private void validateUniqueEmail(String email) {
        String normalizedEmail = normalizeOptional(email);
        if (normalizedEmail != null && guestRepository.existsByEmail(normalizedEmail)) {
            throw new GuestException("Email ja esta cadastrado para outro hospede.");
        }
    }

    private void validateUniqueEmail(String email, Long id) {
        String normalizedEmail = normalizeOptional(email);
        if (normalizedEmail != null && guestRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new GuestException("Email ja esta cadastrado para outro hospede.");
        }
    }

    private void validateUniqueDocumentNumber(String documentNumber) {
        String normalizedDocumentNumber = normalizeOptional(documentNumber);
        if (normalizedDocumentNumber != null && guestRepository.existsByDocumentNumber(normalizedDocumentNumber)) {
            throw new GuestException("Documento ja esta cadastrado para outro hospede.");
        }
    }

    private void validateUniqueDocumentNumber(String documentNumber, Long id) {
        String normalizedDocumentNumber = normalizeOptional(documentNumber);
        if (normalizedDocumentNumber != null && guestRepository.existsByDocumentNumberAndIdNot(normalizedDocumentNumber, id)) {
            throw new GuestException("Documento ja esta cadastrado para outro hospede.");
        }
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
