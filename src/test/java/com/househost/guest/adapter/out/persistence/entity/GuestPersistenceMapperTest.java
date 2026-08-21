package com.househost.guest.adapter.out.persistence.entity;

import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import com.househost.guest.domain.model.GuestType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuestPersistenceMapperTest {

    @Test
    void roundTripsCareTextAndOperationalState() {
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
                GuestType.VIP,
                "Direto",
                "Observacao interna",
                "Sem lactose\nPrefere silencio",
                "Acesso sem degraus"
        );
        guest.restoreOperationalState(
                GuestStatus.WITH_CONFIRMED_BOOKING,
                3,
                new BigDecimal("950.00"),
                LocalDate.of(2026, 7, 15)
        );
        guest.restorePersistenceState(12L, null, List.of(30L), null, null);

        GuestJpaEntity guestJpaEntity = GuestPersistenceMapper.toEntity(guest);
        Guest restoredGuest = GuestPersistenceMapper.toDomain(guestJpaEntity);

        assertEquals("Sem lactose\nPrefere silencio", guestJpaEntity.preferencesAndRestrictions);
        assertEquals("Acesso sem degraus", guestJpaEntity.accessibilityNeeds);
        assertEquals("Sem lactose\nPrefere silencio", restoredGuest.getPreferencesAndRestrictions());
        assertEquals("Acesso sem degraus", restoredGuest.getAccessibilityNeeds());
        assertEquals(GuestStatus.WITH_CONFIRMED_BOOKING, restoredGuest.getStatus());
        assertEquals(3, restoredGuest.getStayCount());
        assertEquals(new BigDecimal("950.00"), restoredGuest.getTotalSpent());
        assertEquals(List.of(30L), restoredGuest.getFinancialTransactionIds());
    }
}
