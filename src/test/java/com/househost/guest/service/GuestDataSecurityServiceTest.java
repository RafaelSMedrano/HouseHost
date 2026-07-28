package com.househost.guest.application.service;

import com.househost.guest.domain.model.Guest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GuestDataSecurityServiceTest {

    private final GuestDataSecurityService service = new GuestDataSecurityService();

    @Test
    void masksAnyGuestDataWithoutChangingMissingValues() {
        assertEquals("***", service.maskData("maria@example.com"));
        assertEquals("***", service.maskData(LocalDate.of(1990, 1, 10)));
        assertEquals("***", service.maskData(12L));
        assertEquals("", service.maskData(""));
        assertNull(service.maskData(null));
    }

    @Test
    void masksAllSensitiveGuestDataWithoutChangingTheOriginalGuest() {
        Guest guest = new Guest("Maria", "maria@example.com", "11999999999", "12345678900");
        ReflectionTestUtils.setField(guest, "id", 12L);
        ReflectionTestUtils.setField(guest, "address", "Rua das Flores, 10");
        ReflectionTestUtils.setField(guest, "birthDate", LocalDate.of(1990, 1, 10));
        ReflectionTestUtils.setField(guest, "notes", "Observacao interna");
        guest.addFinancialTransactionId(30L);

        Guest maskedGuest = service.maskFullData(guest);

        assertEquals(12L, maskedGuest.getId());
        assertEquals("Maria", maskedGuest.getFullName());
        assertEquals("***", maskedGuest.getEmail());
        assertEquals("***", maskedGuest.getPhone());
        assertEquals("***", maskedGuest.getDocumentNumber());
        assertEquals("***", maskedGuest.getAddress());
        assertNull(maskedGuest.getBirthDate());
        assertEquals("***", maskedGuest.getNotes());
        assertEquals(guest.getFinancialTransactionIds(), maskedGuest.getFinancialTransactionIds());

        assertEquals("maria@example.com", guest.getEmail());
        assertEquals(LocalDate.of(1990, 1, 10), guest.getBirthDate());
    }
}
