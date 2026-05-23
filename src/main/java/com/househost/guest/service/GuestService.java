package com.househost.guest.service;

import com.househost.guest.dto.GuestRegisterRequestDTO;
import com.househost.guest.dto.GuestRegisterResponseDTO;
import com.househost.guest.dto.GuestRequestDTO;
import com.househost.guest.dto.GuestResponseDTO;
import com.househost.guest.model.Guest;
import com.househost.guest.model.GuestStatus;
import com.househost.guest.model.GuestType;
import com.househost.guest.repository.GuestRepository;
import com.househost.shared.exception.GuestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public GuestResponseDTO create(GuestRequestDTO request) {
        validateRequest(request);
        validateUniqueEmail(request.email);
        validateUniqueDocumentNumber(request.documentNumber);

        Guest guest = new Guest();
        applyProfile(guest, request);

        Guest savedGuest = guestRepository.save(guest);
        return new GuestResponseDTO(savedGuest);
    }

    public GuestRegisterResponseDTO guestRegister(GuestRegisterRequestDTO request) {
        validateGuestRegisterRequest(request);
        validateUniqueEmail(request.email);
        validateUniqueDocumentNumber(request.documentNumber);

        Guest guest = new Guest();
        applyProfile(guest, request);

        Guest savedGuest = guestRepository.save(guest);
        return new GuestRegisterResponseDTO(savedGuest);
    }

    public List<GuestResponseDTO> findAll() {
        return guestRepository.findAll()
                .stream()
                .map(GuestResponseDTO::new)
                .toList();
    }

    public GuestResponseDTO findById(Long id) {
        Guest guest = findGuestById(id);
        return new GuestResponseDTO(guest);
    }

    public GuestResponseDTO update(Long id, GuestRequestDTO request) {
        validateRequest(request);

        Guest guest = findGuestById(id);
        validateUniqueEmail(request.email, id);
        validateUniqueDocumentNumber(request.documentNumber, id);

        applyProfile(guest, request);

        Guest savedGuest = guestRepository.save(guest);
        return new GuestResponseDTO(savedGuest);
    }

    public void delete(Long id) {
        Guest guest = findGuestById(id);
        guestRepository.delete(guest);
    }

    public Guest findGuestById(Long id) {
        if (id == null) {
            throw new GuestException("Hospede nao encontrado.");
        }

        return guestRepository.findById(id)
                .orElseThrow(() -> new GuestException("Hospede nao encontrado."));
    }

    public Guest refreshFinancialStatus(Guest guest) {
        if (guest == null) {
            return null;
        }

        guest.refreshFinancialStatus();
        return guestRepository.save(guest);
    }

    private void validateRequest(GuestRequestDTO request) {
        if (request == null || isBlank(request.fullName)) {
            throw new GuestException("Nome completo e obrigatorio.");
        }

        validateGuestDetails(request.rating, request.stayCount, request.totalSpent);
    }

    private void validateGuestRegisterRequest(GuestRegisterRequestDTO request) {
        if (request == null || isBlank(request.fullName)) {
            throw new GuestException("Nome completo e obrigatorio.");
        }

        validateGuestDetails(request.rating, request.stayCount, request.totalSpent);
    }

    private void validateGuestDetails(Integer rating, Integer stayCount, BigDecimal totalSpent) {
        if (rating != null && (rating < 0 || rating > 5)) {
            throw new GuestException("Avaliacao do hospede deve estar entre 0 e 5.");
        }

        if (stayCount != null && stayCount < 0) {
            throw new GuestException("Numero de estadias nao pode ser negativo.");
        }

        if (totalSpent != null && totalSpent.signum() < 0) {
            throw new GuestException("Total gasto nao pode ser negativo.");
        }
    }

    private void applyProfile(Guest guest, GuestRequestDTO request) {
        guest.updateProfile(
                normalizeRequired(request.fullName),
                normalizeOptional(request.email),
                normalizeOptional(request.phone),
                normalizeOptional(request.documentNumber),
                normalizeOptional(request.city),
                normalizeOptional(request.state),
                normalizeOptional(request.address),
                request.birthDate,
                normalizeOptional(request.gender),
                normalizeGuestType(request.guestType),
                normalizeStatus(request.status),
                Boolean.TRUE.equals(request.travelsWithPets),
                normalizeOptional(request.petType),
                Boolean.TRUE.equals(request.needsAccessibility),
                normalizeOptional(request.favoriteRoom),
                normalizeNonNegative(request.stayCount),
                normalizeMoney(request.totalSpent),
                request.lastStayDate,
                normalizeRating(request.rating),
                normalizeOptional(request.originChannel),
                normalizeOptional(request.referredBy),
                normalizeOptional(request.notes),
                normalizePreferences(request.preferences)
        );
    }

    private void applyProfile(Guest guest, GuestRegisterRequestDTO request) {
        guest.updateProfile(
                normalizeRequired(request.fullName),
                normalizeOptional(request.email),
                normalizeOptional(request.phone),
                normalizeOptional(request.documentNumber),
                normalizeOptional(request.city),
                normalizeOptional(request.state),
                normalizeOptional(request.address),
                request.birthDate,
                normalizeOptional(request.gender),
                normalizeGuestType(request.guestType),
                normalizeStatus(request.status),
                Boolean.TRUE.equals(request.travelsWithPets),
                normalizeOptional(request.petType),
                Boolean.TRUE.equals(request.needsAccessibility),
                normalizeOptional(request.favoriteRoom),
                normalizeNonNegative(request.stayCount),
                normalizeMoney(request.totalSpent),
                request.lastStayDate,
                normalizeRating(request.rating),
                normalizeOptional(request.originChannel),
                normalizeOptional(request.referredBy),
                normalizeOptional(request.notes),
                normalizePreferences(request.preferences)
        );
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

    private GuestType normalizeGuestType(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return GuestType.REGULAR;
        }

        String key = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase()
                .replace("-", "_")
                .replace(" ", "_");

        return switch (key) {
            case "NOVO", "NEW" -> GuestType.NOVO;
            case "REGULAR" -> GuestType.REGULAR;
            case "VIP" -> GuestType.VIP;
            default -> throw new GuestException("Tipo do hospede invalido. Use NOVO, REGULAR ou VIP.");
        };
    }

    private GuestStatus normalizeStatus(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return GuestStatus.IN_BOOKING;
        }

        String key = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase()
                .replace("-", "_")
                .replace(" ", "_");

        return switch (key) {
            case "IN_BOOKING", "COM_RESERVA", "RESERVA", "BOOKED", "WITH_BOOKING" -> GuestStatus.IN_BOOKING;
            case "IN_STAY", "EM_ESTADIA", "ESTADIA", "HOSPEDADO", "STAYING", "ACTIVE_STAY" -> GuestStatus.IN_STAY;
            case "GOT_CHECKOUT", "GOT_CHECK_OUT", "COM_CHECK_OUT", "COM_CHECKOUT", "CHECK_OUT", "CHECKOUT", "CHECKED_OUT" -> GuestStatus.GOT_CHECKOUT;
            default -> throw new GuestException("Status do hospede invalido. Use IN_BOOKING, IN_STAY ou GOT_CHECKOUT.");
        };
    }

    private Integer normalizeNonNegative(Integer value) {
        return value == null ? null : Math.max(0, value);
    }

    private Integer normalizeRating(Integer value) {
        if (value == null) {
            return null;
        }

        return Math.max(0, Math.min(5, value));
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return null;
        }

        return value.max(BigDecimal.ZERO);
    }

    private List<String> normalizePreferences(List<String> preferences) {
        if (preferences == null) {
            return List.of();
        }

        return preferences.stream()
                .map(this::normalizeOptional)
                .filter(preference -> preference != null)
                .distinct()
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
