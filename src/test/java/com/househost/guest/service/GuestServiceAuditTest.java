package com.househost.guest.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.audit.domain.model.AuditEvent;
import com.househost.audit.application.port.out.AuditEventPersistencePort;
import com.househost.audit.application.service.AuditEventService;
import com.househost.audit.application.service.AuditMetadataService;
import com.househost.audit.application.service.AuditValidationService;
import com.househost.audit.adapter.out.integration.AuditProcessingOperationAdapter;
import com.househost.audit.adapter.out.security.SpringSecurityAuditAdapter;
import com.househost.audit.adapter.out.serialization.JacksonAuditMetadataAdapter;
import com.househost.auth.domain.model.User;
import com.househost.auth.domain.model.UserRole;
import com.househost.auth.application.port.out.UserPersistencePort;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.adapter.out.integration.GuestAuditAdapter;
import com.househost.guest.application.port.out.GuestPersistencePort;
import com.househost.guest.application.port.out.GuestRelationQueryPort;
import com.househost.privacy.processing.application.port.in.DataProcessingOperationUseCase;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.security.application.port.out.SecurityIdentityPort;
import com.househost.security.domain.model.SecurityIdentity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestServiceAuditTest {

    @Mock
    private GuestPersistencePort guestRepository;

    @Mock
    private GuestRelationQueryPort guestRelationQueryPort;

    @Mock
    private AuditEventPersistencePort auditEventRepository;

    @Mock
    private DataProcessingOperationUseCase processingOperationUseCase;

    @Mock
    private UserPersistencePort userRepository;

    private GuestService guestService;
    private GuestDataSecurityService guestDataSecurityService;

    @BeforeEach
    void setUp() {
        SecurityIdentityPort securityIdentityPort = email -> userRepository.findByEmail(email)
                .map(user -> new SecurityIdentity(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole().name()
                ));
        SpringSecurityAuditAdapter actorProvider = new SpringSecurityAuditAdapter(securityIdentityPort);
        AuditEventService auditEventService = new AuditEventService(
                auditEventRepository,
                new AuditProcessingOperationAdapter(processingOperationUseCase),
                actorProvider,
                new AuditValidationService(),
                new AuditMetadataService(new JacksonAuditMetadataAdapter(new ObjectMapper()))
        );
        GuestAuditAdapter guestAuditAdapter = new GuestAuditAdapter(auditEventService);
        guestDataSecurityService = new GuestDataSecurityService();
        guestService = new GuestService(
                guestRepository,
                guestAuditAdapter,
                guestRelationQueryPort,
                new GuestValidationService(guestRepository),
                guestDataSecurityService
        );

        User user = new User("Administrador", "admin@househost.com", "hash", UserRole.ADMIN);
        ReflectionTestUtils.setField(user, "id", 7L);
        when(userRepository.findByEmail("admin@househost.com")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@househost.com", null, List.of())
        );

    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditsGuestViewDirectlyInsideService() {
        Guest guest = new Guest("Maria", "maria@example.com", "11999999999", null);
        ReflectionTestUtils.setField(guest, "id", 12L);
        guest.prePersist();

        when(guestRepository.findById(12L)).thenReturn(Optional.of(guest));
        when(processingOperationUseCase.findIdByOperationCode(
                DataProcessingOperationCodes.GUEST_MANAGEMENT
        )).thenReturn(Optional.of(21L));

        guestService.findById(12L, true);

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();

        assertEquals("GUEST_VIEWED", event.getEventType());
        assertEquals("GUEST", event.getEntityType());
        assertEquals(12L, event.getEntityId());
        assertEquals(7L, event.getActorId());
        assertEquals(21L, event.getProcessingOperationId());
    }

    @Test
    void masksContactUntilExplicitRevealOrEditAccess() {
        Guest guest = new Guest("Maria", "maria@example.com", "11999990019", null);
        ReflectionTestUtils.setField(guest, "id", 12L);
        ReflectionTestUtils.setField(guest, "documentNumber", "123.456.789-09");
        ReflectionTestUtils.setField(guest, "address", "Rua das Flores, 10");
        ReflectionTestUtils.setField(guest, "birthDate", LocalDate.of(1990, 1, 10));
        ReflectionTestUtils.setField(guest, "notes", "Observacao interna");
        guest.prePersist();

        when(guestRepository.findAll()).thenReturn(List.of(guest));
        when(guestRelationQueryPort.findBookingIdsByGuestIds(List.of(12L))).thenReturn(Map.of());
        when(guestRepository.findById(12L)).thenReturn(Optional.of(guest));
        when(processingOperationUseCase.findIdByOperationCode(
                DataProcessingOperationCodes.GUEST_MANAGEMENT
        )).thenReturn(Optional.of(21L));

        var listedGuest = guestService.findAll(true).get(0);
        var detailedGuest = guestService.findById(12L, true);
        var revealedContact = guestService.revealContact(12L);
        var editableGuest = guestService.findByIdForEdit(12L);

        assertEquals("***", listedGuest.getEmail());
        assertEquals("***", listedGuest.getPhone());
        assertEquals("***", detailedGuest.getEmail());
        assertEquals("***", detailedGuest.getPhone());
        assertEquals("***", detailedGuest.getDocumentNumber());
        assertEquals("***", detailedGuest.getAddress());
        assertNull(detailedGuest.getBirthDate());
        assertEquals("***", detailedGuest.getNotes());
        assertEquals("maria@example.com", revealedContact.getEmail());
        assertEquals("11999990019", revealedContact.getPhone());
        assertEquals("maria@example.com", editableGuest.getEmail());
        assertEquals("11999990019", editableGuest.getPhone());
        assertEquals("123.456.789-09", editableGuest.getDocumentNumber());
        assertEquals("Rua das Flores, 10", editableGuest.getAddress());
        assertEquals(LocalDate.of(1990, 1, 10), editableGuest.getBirthDate());
        assertEquals("Observacao interna", editableGuest.getNotes());

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository, times(4)).save(eventCaptor.capture());
        assertEquals(
                List.of(
                        "GUEST_LIST_VIEWED",
                        "GUEST_VIEWED",
                        "GUEST_CONTACT_REVEALED",
                        "GUEST_EDIT_DATA_VIEWED"
                ),
                eventCaptor.getAllValues().stream().map(AuditEvent::getEventType).toList()
        );
    }
}
