package com.househost.guest.application.service;

import com.househost.guest.application.port.in.GuestUseCase;
import com.househost.guest.application.dto.GuestContactResponseDTO;
import com.househost.guest.application.dto.GuestLookupResponseDTO;
import com.househost.guest.application.dto.GuestRegisterRequestDTO;
import com.househost.guest.application.dto.GuestRegisterResponseDTO;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import com.househost.guest.domain.model.GuestType;
import com.househost.guest.application.port.out.GuestAuditPort;
import com.househost.guest.application.port.out.GuestPersistencePort;
import com.househost.guest.application.port.out.GuestRelationQueryPort;
import com.househost.shared.exception.GuestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GuestService implements GuestUseCase {

    private final GuestPersistencePort guestRepository;
    private final GuestAuditPort guestAuditPort;
    private final GuestRelationQueryPort guestRelationQueryPort;
    private final GuestValidationService guestValidationService;
    private final GuestDataSecurityService guestDataSecurityService;

    public GuestService(
            GuestPersistencePort guestRepository,
            GuestAuditPort guestAuditPort,
            GuestRelationQueryPort guestRelationQueryPort,
            GuestValidationService guestValidationService,
            GuestDataSecurityService guestDataSecurityService
    ) {
        this.guestRepository = guestRepository;
        this.guestAuditPort = guestAuditPort;
        this.guestRelationQueryPort = guestRelationQueryPort;
        this.guestValidationService = guestValidationService;
        this.guestDataSecurityService = guestDataSecurityService;
    }

    public GuestRegisterResponseDTO guestRegister(GuestRegisterRequestDTO request) {
        guestValidationService.validateRegistration(request);

        Guest guest = new Guest();
        applyProfile(guest, request);

        Guest savedGuest = guestRepository.save(guest);
        guestAuditPort.record("GUEST_CREATED", savedGuest.getId(), Map.of("source", "REGISTER"));
        return toResponse(savedGuest);
    }

    public List<GuestRegisterResponseDTO> findAll(boolean masked) {
        List<Guest> guests = guestRepository.findAll();
        List<Long> guestIds = guests.stream().map(Guest::getId).toList();
        Map<Long, List<Long>> bookingIds = guestRelationQueryPort.findBookingIdsByGuestIds(guestIds);
        List<GuestRegisterResponseDTO> response = guests.stream()
                .map(guest -> toResponse(
                        masked ? maskFullData(guest) : guest,
                        bookingIds.getOrDefault(guest.getId(), List.of())
                ))
                .toList();
        guestAuditPort.record("GUEST_LIST_VIEWED", null, Map.of("resultCount", response.size(), "masked", masked));
        return response;
    }

    public List<Guest> findAllGuests() {
        return guestRepository.findAll();
    }

    public GuestRegisterResponseDTO findById(Long id, boolean masked) {
        Guest guest = findGuestById(id);
        guestAuditPort.record("GUEST_VIEWED", guest.getId(), Map.of("masked", masked));
        return toResponse(masked ? maskFullData(guest) : guest);
    }

    public List<GuestLookupResponseDTO> findGuestsByName(String name) {
        String normalizedName = normalizeOptional(name);
        if (normalizedName == null) {
            return List.of();
        }
        List<GuestLookupResponseDTO> response = guestRepository.findByFullNameContaining(normalizedName)
                .stream()
                .map(GuestLookupResponseDTO::new)
                .toList();
        guestAuditPort.record("GUEST_NAME_LOOKUP_VIEWED", null, Map.of("resultCount", response.size()));
        return response;
    }

    public List<GuestLookupResponseDTO> findGuestsByDocumentNumber(String documentNumber) {
        String normalizedDocumentNumber = normalizeOptional(documentNumber);
        if (normalizedDocumentNumber == null) {
            return List.of();
        }
        List<GuestLookupResponseDTO> response = guestRepository
                .findByDocumentNumberContaining(normalizedDocumentNumber)
                .stream()
                .map(GuestLookupResponseDTO::new)
                .toList();
        guestAuditPort.record("GUEST_DOCUMENT_LOOKUP_VIEWED", null, Map.of("resultCount", response.size()));
        return response;
    }

    public List<GuestLookupResponseDTO> findGuestsByEmail(String email) {
        String normalizedEmail = normalizeOptional(email);
        if (normalizedEmail == null) {
            return List.of();
        }
        return findMaskedLookupResults(guestRepository.findByEmailContaining(normalizedEmail), "GUEST_EMAIL_LOOKUP_VIEWED");
    }

    public List<GuestLookupResponseDTO> findGuestsByPhone(String phone) {
        String normalizedPhone = normalizeOptional(phone);
        if (normalizedPhone == null) {
            return List.of();
        }
        return findMaskedLookupResults(guestRepository.findByPhoneContaining(normalizedPhone), "GUEST_PHONE_LOOKUP_VIEWED");
    }

    public List<GuestLookupResponseDTO> findGuestsByCity(String city) {
        String normalizedCity = normalizeOptional(city);
        if (normalizedCity == null) {
            return List.of();
        }
        return findMaskedLookupResults(guestRepository.findByCityContaining(normalizedCity), "GUEST_CITY_LOOKUP_VIEWED");
    }

    private List<GuestLookupResponseDTO> findMaskedLookupResults(List<Guest> guests, String eventType) {
        List<GuestLookupResponseDTO> response = guests.stream()
                .map(guest -> new GuestLookupResponseDTO(guest.getId(), guest.getFullName(), null))
                .toList();
        guestAuditPort.record(eventType, null, Map.of("resultCount", response.size()));
        return response;
    }

    public GuestRegisterResponseDTO findByIdForEdit(Long id) {
        Guest guest = findGuestById(id);
        guestAuditPort.record("GUEST_EDIT_DATA_VIEWED", guest.getId(), Map.of());
        return toResponse(guest);
    }

    public GuestContactResponseDTO revealContact(Long id) {
        Guest guest = findGuestById(id);
        guestAuditPort.record("GUEST_CONTACT_REVEALED", guest.getId(), Map.of());
        return new GuestContactResponseDTO(guest.getId(), guest.getEmail(), guest.getPhone());
    }

    public String maskData(Object data) {
        return guestDataSecurityService.maskData(data);
    }

    public Guest maskFullData(Guest guest) {
        return guestDataSecurityService.maskFullData(guest);
    }

    public GuestRegisterResponseDTO update(Long id, GuestRegisterRequestDTO request) {
        guestValidationService.validateUpdate(id, request);
        Guest guest = findGuestById(id);

        applyProfile(guest, request);

        Guest savedGuest = guestRepository.save(guest);
        guestAuditPort.record("GUEST_UPDATED", savedGuest.getId(), Map.of());
        return toResponse(savedGuest);
    }

    public GuestRegisterResponseDTO delete(Long id) {
        Guest guest = findGuestById(id);
        GuestRegisterResponseDTO response = toResponse(guest);
        guestRepository.delete(guest);
        guestAuditPort.record("GUEST_DELETED", guest.getId(), Map.of());
        return response;
    }

    private GuestRegisterResponseDTO toResponse(Guest guest) {
        return toResponse(
                guest,
                guestRelationQueryPort.findBookingIds(guest.getId())
        );
    }

    private GuestRegisterResponseDTO toResponse(
            Guest guest,
            List<Long> bookingIds
    ) {
        return new GuestRegisterResponseDTO(guest, bookingIds);
    }

    public Guest findGuestById(Long id) {
        if (id == null) {
            throw new GuestException("Hospede nao encontrado.");
        }

        return guestRepository.findById(id)
                .orElseThrow(() -> new GuestException("Hospede nao encontrado."));
    }

    public Guest changeStatus(Long id, GuestStatus status) {
        Guest guest = findGuestById(id);
        guest.changeStatus(status);
        return guestRepository.save(guest);
    }

    public Guest findGuestByDocumentNumber(String documentNumber) {
        String normalizedDocumentNumber = normalizeOptional(documentNumber);
        if (normalizedDocumentNumber == null) {
            throw new GuestException("Documento do hospede e obrigatorio.");
        }

        return guestRepository.findByDocumentNumber(normalizedDocumentNumber)
                .orElseThrow(() -> new GuestException(
                        "Hospede cadastrado nao encontrado pelo CPF informado."
                ));
    }

    public Guest findUniqueGuestByFullName(String fullName) {
        String normalizedFullName = normalizeOptional(fullName);
        if (normalizedFullName == null) {
            throw new GuestException("Nome do hospede e obrigatorio.");
        }

        List<Guest> guests = guestRepository.findByFullNameIgnoreCase(normalizedFullName);
        if (guests.isEmpty()) {
            throw new GuestException("Hospede cadastrado nao encontrado pelo nome informado.");
        }

        if (guests.size() > 1) {
            throw new GuestException(
                    "Mais de um hospede cadastrado com este nome. Informe o CPF para identificar corretamente."
            );
        }

        return guests.get(0);
    }

    private void applyProfile(Guest guest, GuestRegisterRequestDTO request) {
        guest.updateProfile(
                request.fullName.trim(),
                normalizeOptional(request.email),
                normalizeOptional(request.phone),
                normalizeOptional(request.documentNumber),
                normalizeOptional(request.city),
                normalizeOptional(request.state),
                normalizeOptional(request.address),
                request.birthDate,
                normalizeOptional(request.gender),
                request.guestType == null ? GuestType.REGULAR : request.guestType,
                request.status == null ? GuestStatus.IN_BOOKING : request.status,
                Boolean.TRUE.equals(request.travelsWithPets),
                normalizeOptional(request.petType),
                Boolean.TRUE.equals(request.needsAccessibility),
                normalizeOptional(request.favoriteRoom),
                request.stayCount,
                request.totalSpent,
                request.lastStayDate,
                request.rating,
                normalizeOptional(request.originChannel),
                normalizeOptional(request.referredBy),
                normalizeOptional(request.notes),
                normalizePreferences(request.preferences)
        );
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

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
