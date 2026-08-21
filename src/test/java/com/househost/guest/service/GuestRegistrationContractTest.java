package com.househost.guest.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.guest.application.dto.GuestRegisterRequestDTO;
import com.househost.guest.application.dto.GuestRegisterResponseDTO;
import com.househost.guest.application.port.out.GuestAuditPort;
import com.househost.guest.application.port.out.GuestPersistencePort;
import com.househost.guest.application.port.out.GuestRelationQueryPort;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import com.househost.shared.exception.GuestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GuestRegistrationContractTest {

    @Mock
    private GuestPersistencePort guestPersistencePort;

    @Mock
    private GuestAuditPort guestAuditPort;

    @Mock
    private GuestRelationQueryPort guestRelationQueryPort;

    private GuestService guestService;

    @BeforeEach
    void setUp() {
        GuestValidationService guestValidationService =
                new GuestValidationService(guestPersistencePort);
        GuestDataSecurityService guestDataSecurityService = new GuestDataSecurityService();
        guestService = new GuestService(
                guestPersistencePort,
                guestAuditPort,
                guestRelationQueryPort,
                guestValidationService,
                guestDataSecurityService
        );
        lenient().when(guestPersistencePort.save(any(Guest.class))).thenAnswer(invocation -> {
            Guest savedGuest = invocation.getArgument(0);
            if (savedGuest.getId() == null) {
                savedGuest.restorePersistenceState(15L, null, List.of(), null, null);
            }
            return savedGuest;
        });
        lenient().when(guestRelationQueryPort.findBookingIds(anyLong())).thenReturn(List.of());
    }

    @Test
    void legacyWriteMembersAreIgnoredAndRegistrationAlwaysStartsInactive() throws Exception {
        GuestRegisterRequestDTO guestRegisterRequestDTO = new ObjectMapper().readValue("""
                {
                  "fullName": "  Maria Silva  ",
                  "phone": "11999999999",
                  "status": "IN_STAY",
                  "stayCount": 99,
                  "totalSpent": 99999,
                  "lastStayDate": "2026-08-10",
                  "travelsWithPets": true,
                  "petType": "Cachorro",
                  "needsAccessibility": true,
                  "favoriteRoom": "Lavanda",
                  "preferences": ["Legado"],
                  "preferencesAndRestrictions": "  Sem lactose\\nQuarto silencioso  ",
                  "accessibilityNeeds": "  Acesso sem degraus  ",
                  "notes": "  Conteudo interno  "
                }
                """, GuestRegisterRequestDTO.class);

        GuestRegisterResponseDTO guestRegisterResponseDTO =
                guestService.guestRegister(guestRegisterRequestDTO);

        ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
        verify(guestPersistencePort).save(guestCaptor.capture());
        Guest savedGuest = guestCaptor.getValue();
        assertEquals(GuestStatus.INACTIVE, savedGuest.getStatus());
        assertNull(savedGuest.getStayCount());
        assertNull(savedGuest.getTotalSpent());
        assertNull(savedGuest.getLastStayDate());
        assertEquals("Sem lactose\nQuarto silencioso", savedGuest.getPreferencesAndRestrictions());
        assertEquals("Acesso sem degraus", savedGuest.getAccessibilityNeeds());
        assertEquals("Sem lactose\nQuarto silencioso",
                guestRegisterResponseDTO.getPreferencesAndRestrictions());
        assertEquals("Acesso sem degraus", guestRegisterResponseDTO.getAccessibilityNeeds());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> metadataMapCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(guestAuditPort).record(eq("GUEST_CREATED"), eq(15L), metadataMapCaptor.capture());
        String auditMetadata = metadataMapCaptor.getValue().toString();
        assertFalse(auditMetadata.contains("Sem lactose"));
        assertFalse(auditMetadata.contains("Acesso sem degraus"));
        assertFalse(auditMetadata.contains("Conteudo interno"));
    }

    @Test
    void ordinaryUpdatePreservesLifecycleAndHistory() {
        Guest existingGuest = existingGuest();
        when(guestPersistencePort.findById(12L)).thenReturn(java.util.Optional.of(existingGuest));
        GuestRegisterRequestDTO guestRegisterRequestDTO = request("Maria atualizada");
        guestRegisterRequestDTO.preferencesAndRestrictions = "Nova preferencia";
        guestRegisterRequestDTO.accessibilityNeeds = "Nova necessidade";

        GuestRegisterResponseDTO guestRegisterResponseDTO =
                guestService.update(12L, guestRegisterRequestDTO);

        assertEquals(GuestStatus.WITH_CONFIRMED_BOOKING, existingGuest.getStatus());
        assertEquals(7, existingGuest.getStayCount());
        assertEquals(new BigDecimal("2300.75"), existingGuest.getTotalSpent());
        assertEquals(LocalDate.of(2026, 8, 1), existingGuest.getLastStayDate());
        assertEquals("Nova preferencia", guestRegisterResponseDTO.getPreferencesAndRestrictions());
        assertEquals("Nova necessidade", guestRegisterResponseDTO.getAccessibilityNeeds());
    }

    @Test
    void rejectsCareTextBeyondTheDocumentedLimitWithoutTruncation() {
        GuestRegisterRequestDTO guestRegisterRequestDTO = request("Maria Silva");
        guestRegisterRequestDTO.preferencesAndRestrictions =
                "a".repeat(GuestValidationService.MAX_CARE_TEXT_LENGTH + 1);

        GuestException guestException = assertThrows(
                GuestException.class,
                () -> guestService.guestRegister(guestRegisterRequestDTO)
        );

        assertEquals(
                "Preferencias e restricoes deve ter no maximo 4000 caracteres.",
                guestException.getMessage()
        );
    }

    @Test
    void requestContractContainsOnlyEditableGuestProfileMembers() {
        Set<String> requestFieldNameSet = Arrays.stream(
                        GuestRegisterRequestDTO.class.getDeclaredFields()
                )
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "fullName",
                "email",
                "phone",
                "documentNumber",
                "city",
                "state",
                "address",
                "birthDate",
                "gender",
                "guestType",
                "originChannel",
                "notes",
                "preferencesAndRestrictions",
                "accessibilityNeeds"
        ), requestFieldNameSet);
    }

    @Test
    void responseContractContainsNoGuestPetAssociation() {
        Set<String> responseFieldNameSet = Arrays.stream(
                        GuestRegisterResponseDTO.class.getDeclaredFields()
                )
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toSet());

        assertFalse(responseFieldNameSet.contains("travelsWithPets"));
        assertFalse(responseFieldNameSet.contains("petType"));
        assertFalse(responseFieldNameSet.contains("referredBy"));
        assertFalse(responseFieldNameSet.contains("rating"));
    }

    private Guest existingGuest() {
        Guest guest = new Guest();
        guest.updateProfile(
                "Maria Silva",
                "maria@example.com",
                "11999999999",
                "12345678900",
                "Cunha",
                "SP",
                "Rua das Flores, 10",
                LocalDate.of(1990, 1, 10),
                "Feminino",
                null,
                "Direto",
                "Observacao",
                "Preferencia antiga",
                "Necessidade antiga"
        );
        guest.restoreOperationalState(
                GuestStatus.WITH_CONFIRMED_BOOKING,
                7,
                new BigDecimal("2300.75"),
                LocalDate.of(2026, 8, 1)
        );
        guest.restorePersistenceState(12L, null, List.of(), null, null);
        return guest;
    }

    private GuestRegisterRequestDTO request(String fullName) {
        GuestRegisterRequestDTO guestRegisterRequestDTO = new GuestRegisterRequestDTO();
        guestRegisterRequestDTO.fullName = fullName;
        guestRegisterRequestDTO.phone = "11999999999";
        return guestRegisterRequestDTO;
    }
}
