package com.househost.publicapi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.application.port.out.GuestPersistencePort;
import com.househost.guest.domain.model.Guest;
import com.househost.privacy.policy.application.port.in.PublicPrivacyPolicyUseCase;
import com.househost.privacy.policy.application.records.PublishedPrivacyPolicyRecord;
import com.househost.privacy.policy.domain.exception.PrivacyPolicyConflictException;
import com.househost.publicapi.application.dto.PublicBookingRequestDTO;
import com.househost.publicapi.application.dto.PublicBookingResponseDTO;
import com.househost.publicapi.application.dto.PublicQuoteRequestDTO;
import com.househost.publicapi.application.port.out.PublicBookingAuditPort;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import com.househost.shared.exception.BookingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PublicBookingServiceTest {

    private final RoomService roomService = mock(RoomService.class);
    private final BookingPersistencePort bookingPersistencePort = mock(BookingPersistencePort.class);
    private final GuestPersistencePort guestPersistencePort = mock(GuestPersistencePort.class);
    private final PublicBookingAuditPort publicBookingAuditPort = mock(PublicBookingAuditPort.class);
    private final PublicPrivacyPolicyUseCase publicPrivacyPolicyUseCase =
            mock(PublicPrivacyPolicyUseCase.class);
    private PublicBookingService publicBookingService;

    @BeforeEach
    void setUp() {
        publicBookingService = new PublicBookingService(
                roomService,
                bookingPersistencePort,
                guestPersistencePort,
                publicBookingAuditPort,
                publicPrivacyPolicyUseCase
        );
        Room room = new Room("Casa", RoomType.DOUBLE, 4, new BigDecimal("350.00"), RoomStatus.AVAILABLE);
        room.restorePersistenceState(1L, null, null);
        when(roomService.findRoomById(1L)).thenReturn(room);
        when(bookingPersistencePort.findOverlappingBookings(
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class),
                anyCollection()
        )).thenReturn(List.of());
        when(guestPersistencePort.save(any(Guest.class))).thenAnswer(invocation -> {
            Guest guest = invocation.getArgument(0);
            guest.restorePersistenceState(7L, null, List.of(), null, null);
            return guest;
        });
        when(bookingPersistencePort.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.restorePersistenceState(
                    9L,
                    booking.getPaymentStatus(),
                    booking.getPrivacyPolicyVersion(),
                    booking.getPrivacyPolicyContentHash(),
                    booking.getTermsVersion(),
                    booking.getPrivacyAcceptedAt(),
                    booking.getMarketingOptIn(),
                    booking.getMarketingOptInAt(),
                    null,
                    null
            );
            return booking;
        });
        when(publicPrivacyPolicyUseCase.requireCurrentPublishedForAcceptance(2L))
                .thenReturn(new PublishedPrivacyPolicyRecord(
                        2L,
                        2,
                        "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                        LocalDateTime.of(2026, 7, 26, 0, 0)
                ));
    }

    @Test
    void createsBookingWithNormalizedMinimalDataAndNumericComposition() {
        PublicBookingRequestDTO request = validRequest();
        request.guest.firstName = "  Maria   Clara ";
        request.guest.phone = "(12) 99999-9999";

        PublicBookingResponseDTO response = publicBookingService.createBooking(request);

        assertEquals("CL-9", response.bookingCode());
        assertEquals("Maria Clara", request.guest.firstName);
        assertEquals("+5512999999999", request.guest.phone);
        Booking savedBooking = org.mockito.Mockito.mockingDetails(bookingPersistencePort)
                .getInvocations()
                .stream()
                .filter(invocation -> invocation.getMethod().getName().equals("save"))
                .map(invocation -> (Booking) invocation.getArgument(0))
                .findFirst()
                .orElseThrow();
        assertEquals(2, savedBooking.getAdults());
        assertEquals(1, savedBooking.getChildren());
        assertEquals(1, savedBooking.getPets());
        assertFalse(Boolean.TRUE.equals(savedBooking.getMarketingOptIn()));
        assertNull(savedBooking.getPaymentMethod());
        assertEquals("+5512999999999", savedBooking.getGuest().getPhone());
        assertNull(savedBooking.getGuest().getEmail());
        assertNull(savedBooking.getGuest().getDocumentNumber());
        assertEquals("2", savedBooking.getPrivacyPolicyVersion());
        assertEquals(
                "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                savedBooking.getPrivacyPolicyContentHash()
        );
        assertNotNull(savedBooking.getPrivacyAcceptedAt());
    }

    @Test
    void rejectsInvalidNamePhoneNotesAndComposition() {
        PublicBookingRequestDTO invalidNameRequest = validRequest();
        invalidNameRequest.guest.firstName = "7";
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(invalidNameRequest));

        PublicBookingRequestDTO invalidPhoneRequest = validRequest();
        invalidPhoneRequest.guest.phone = "123";
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(invalidPhoneRequest));

        PublicBookingRequestDTO oversizedNotesRequest = validRequest();
        oversizedNotesRequest.notes = "x".repeat(501);
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(oversizedNotesRequest));

        PublicBookingRequestDTO invalidCompositionRequest = validRequest();
        invalidCompositionRequest.adults = 0;
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(invalidCompositionRequest));
    }

    @Test
    void rejectsEveryConfiguredTextAndCountOverflow() {
        PublicBookingRequestDTO firstNameRequest = validRequest();
        firstNameRequest.guest.firstName = "a".repeat(81);
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(firstNameRequest));

        PublicBookingRequestDTO lastNameRequest = validRequest();
        lastNameRequest.guest.lastName = "a".repeat(81);
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(lastNameRequest));

        PublicBookingRequestDTO phoneRequest = validRequest();
        phoneRequest.guest.phone = "1".repeat(33);
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(phoneRequest));

        PublicBookingRequestDTO cityRequest = validRequest();
        cityRequest.guest.city = "a".repeat(121);
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(cityRequest));

        PublicBookingRequestDTO missingPolicyRequest = validRequest();
        missingPolicyRequest.privacyPolicyId = null;
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(missingPolicyRequest));

        PublicBookingRequestDTO termsVersionRequest = validRequest();
        termsVersionRequest.termsVersion = "a".repeat(101);
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(termsVersionRequest));

        PublicBookingRequestDTO adultsRequest = validRequest();
        adultsRequest.adults = 21;
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(adultsRequest));

        PublicBookingRequestDTO childrenRequest = validRequest();
        childrenRequest.children = 21;
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(childrenRequest));

        PublicBookingRequestDTO totalGuestsRequest = validRequest();
        totalGuestsRequest.adults = 11;
        totalGuestsRequest.children = 10;
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(totalGuestsRequest));

        PublicBookingRequestDTO petsRequest = validRequest();
        petsRequest.pets = 6;
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(petsRequest));
    }

    @Test
    void acceptsCountryCodeAndPersistsOneCanonicalBrazilianPhone() {
        PublicBookingRequestDTO request = validRequest();
        request.guest.phone = "+55 (12) 99999-9999";

        publicBookingService.createBooking(request);

        assertEquals("+5512999999999", request.guest.phone);
    }

    @Test
    void rejectsKnownCpfAndCardPatternsInRemainingFreeText() {
        PublicBookingRequestDTO cpfRequest = validRequest();
        cpfRequest.notes = "Meu CPF e 123.456.789-00";
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(cpfRequest));

        PublicBookingRequestDTO cardRequest = validRequest();
        cardRequest.notes = "Cartao 4111 1111 1111 1111";
        assertThrows(BookingException.class, () -> publicBookingService.createBooking(cardRequest));
    }

    @Test
    void quoteUsesNumericCompositionAndRoomCapacity() {
        PublicQuoteRequestDTO request = new PublicQuoteRequestDTO();
        request.roomId = 1L;
        request.checkIn = LocalDate.now().plusDays(1);
        request.checkOut = LocalDate.now().plusDays(3);
        request.adults = 2;
        request.children = 1;
        request.pets = 0;

        assertEquals(new BigDecimal("700.00"), publicBookingService.quote(request).total());

        request.children = 3;
        assertThrows(BookingException.class, () -> publicBookingService.quote(request));
    }

    @Test
    void publicDtosExcludeDocumentEmailPaymentAndMarketing() {
        Set<String> bookingRequestFieldSet = Arrays.stream(PublicBookingRequestDTO.class.getFields())
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toSet());
        Set<String> guestFieldSet = Arrays.stream(PublicBookingRequestDTO.GuestData.class.getFields())
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toSet());
        Set<String> bookingResponseComponentSet = Arrays.stream(PublicBookingResponseDTO.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .collect(Collectors.toSet());

        assertFalse(bookingRequestFieldSet.contains("paymentMethod"));
        assertFalse(bookingRequestFieldSet.contains("marketingOptIn"));
        assertFalse(bookingRequestFieldSet.contains("privacyPolicyVersion"));
        assertFalse(bookingRequestFieldSet.contains("privacyPolicyContentHash"));
        assertFalse(guestFieldSet.contains("document"));
        assertFalse(guestFieldSet.contains("email"));
        assertFalse(bookingResponseComponentSet.contains("paymentMethod"));
    }

    @Test
    void rejectsChangedPolicyBeforePersistingGuestOrBooking() {
        when(publicPrivacyPolicyUseCase.requireCurrentPublishedForAcceptance(2L))
                .thenThrow(new PrivacyPolicyConflictException("Politica atualizada."));

        assertThrows(
                PrivacyPolicyConflictException.class,
                () -> publicBookingService.createBooking(validRequest())
        );
        verify(guestPersistencePort, never()).save(any(Guest.class));
        verify(bookingPersistencePort, never()).save(any(Booking.class));
    }

    @Test
    void acceptanceAuditContainsSnapshotButNoPolicyRelationshipOrGuestData() {
        publicBookingService.createBooking(validRequest());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> metadataMapCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(publicBookingAuditPort, times(2)).recordBookingEvent(
                any(), any(), any(), any(), any(), any(), any(), metadataMapCaptor.capture()
        );
        Map<String, Object> acceptanceMetadataMap = metadataMapCaptor.getAllValues().get(1);
        assertEquals("2", acceptanceMetadataMap.get("privacyPolicyVersion"));
        assertEquals(
                "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                acceptanceMetadataMap.get("privacyPolicyContentHash")
        );
        assertFalse(acceptanceMetadataMap.containsKey("privacyPolicyId"));
        assertFalse(acceptanceMetadataMap.containsKey("phone"));
        assertFalse(acceptanceMetadataMap.containsKey("content"));
    }

    private PublicBookingRequestDTO validRequest() {
        PublicBookingRequestDTO request = new PublicBookingRequestDTO();
        request.roomId = 1L;
        request.checkIn = LocalDate.now().plusDays(1);
        request.checkOut = LocalDate.now().plusDays(3);
        request.adults = 2;
        request.children = 1;
        request.pets = 1;
        request.privacyPolicyId = 2L;
        request.termsVersion = "2026-06-04-public-pre-reserva";
        request.privacyAccepted = true;
        request.notes = "Precisamos de um berco.";
        request.guest = new PublicBookingRequestDTO.GuestData();
        request.guest.firstName = "Maria";
        request.guest.lastName = "Silva";
        request.guest.phone = "(12) 99999-9999";
        request.guest.city = "Sao Paulo - SP";
        return request;
    }
}
