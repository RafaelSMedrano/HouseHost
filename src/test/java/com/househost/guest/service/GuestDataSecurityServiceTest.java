package com.househost.guest.application.service;

import com.househost.guest.domain.model.Guest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GuestDataSecurityServiceTest {

    private final GuestDataSecurityService guestDataSecurityService =
            new GuestDataSecurityService();

    @Test
    void masksAnyGuestDataWithoutChangingMissingValues() {
        assertEquals("***", guestDataSecurityService.maskData("maria@example.com"));
        assertEquals("***", guestDataSecurityService.maskData(LocalDate.of(1990, 1, 10)));
        assertEquals("***", guestDataSecurityService.maskData(12L));
        assertEquals("", guestDataSecurityService.maskData(""));
        assertNull(guestDataSecurityService.maskData(null));
    }

    @Test
    void masksAllSensitiveGuestDataWithoutChangingTheOriginalGuest() {
        Guest guest = new Guest("Maria", "maria@example.com", "11999999999", "12345678900");
        ReflectionTestUtils.setField(guest, "id", 12L);
        ReflectionTestUtils.setField(guest, "address", "Rua das Flores, 10");
        ReflectionTestUtils.setField(guest, "birthDate", LocalDate.of(1990, 1, 10));
        ReflectionTestUtils.setField(guest, "notes", "Observacao interna");
        ReflectionTestUtils.setField(
                guest,
                "preferencesAndRestrictions",
                "Sem lactose"
        );
        ReflectionTestUtils.setField(
                guest,
                "accessibilityNeeds",
                "Acesso sem degraus"
        );
        guest.addFinancialTransactionId(30L);

        Guest maskedGuest = guestDataSecurityService.maskFullData(guest);

        assertEquals(12L, maskedGuest.getId());
        assertEquals("Maria", maskedGuest.getFullName());
        assertEquals("***", maskedGuest.getEmail());
        assertEquals("***", maskedGuest.getPhone());
        assertEquals("***", maskedGuest.getDocumentNumber());
        assertEquals("***", maskedGuest.getAddress());
        assertNull(maskedGuest.getBirthDate());
        assertEquals("***", maskedGuest.getNotes());
        assertEquals("***", maskedGuest.getPreferencesAndRestrictions());
        assertEquals("***", maskedGuest.getAccessibilityNeeds());
        assertEquals(guest.getFinancialTransactionIds(), maskedGuest.getFinancialTransactionIds());

        assertEquals("maria@example.com", guest.getEmail());
        assertEquals(LocalDate.of(1990, 1, 10), guest.getBirthDate());
        assertEquals("Sem lactose", guest.getPreferencesAndRestrictions());
        assertEquals("Acesso sem degraus", guest.getAccessibilityNeeds());
    }
}
