package com.househost.guest.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GuestProfileTest {

    @Test
    void newGuestStartsInactiveWithNoOperationalHistory() {
        Guest guest = new Guest();

        assertEquals(GuestStatus.INACTIVE, guest.getStatus());
        assertNull(guest.getStayCount());
        assertNull(guest.getTotalSpent());
        assertNull(guest.getLastStayDate());
    }

    @Test
    void profileUpdatePreservesLifecycleAndOperationalHistory() {
        Guest guest = new Guest();
        LocalDate lastStayDate = LocalDate.of(2026, 8, 10);
        guest.restoreOperationalState(
                GuestStatus.IN_STAY,
                4,
                new BigDecimal("1800.50"),
                lastStayDate
        );

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
                GuestType.VIP,
                "Direto",
                "Observacao interna",
                "Prefere ambiente silencioso",
                "Necessita acesso sem degraus"
        );

        assertEquals(GuestStatus.IN_STAY, guest.getStatus());
        assertEquals(4, guest.getStayCount());
        assertEquals(new BigDecimal("1800.50"), guest.getTotalSpent());
        assertEquals(lastStayDate, guest.getLastStayDate());
        assertEquals("Prefere ambiente silencioso", guest.getPreferencesAndRestrictions());
        assertEquals("Necessita acesso sem degraus", guest.getAccessibilityNeeds());
    }

    @Test
    void completedStayAccumulatesOperationalHistory() {
        Guest guest = new Guest();
        guest.restoreOperationalState(
                GuestStatus.IN_STAY,
                2,
                new BigDecimal("700.00"),
                LocalDate.of(2026, 7, 20)
        );

        guest.applyCompletedStay(
                LocalDate.of(2026, 8, 12),
                new BigDecimal("350.00")
        );

        assertEquals(3, guest.getStayCount());
        assertEquals(new BigDecimal("1050.00"), guest.getTotalSpent());
        assertEquals(LocalDate.of(2026, 8, 12), guest.getLastStayDate());
    }
}
