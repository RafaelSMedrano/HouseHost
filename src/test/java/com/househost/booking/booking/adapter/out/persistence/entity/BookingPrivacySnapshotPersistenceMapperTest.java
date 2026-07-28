package com.househost.booking.booking.adapter.out.persistence.entity;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookingPrivacySnapshotPersistenceMapperTest {
    @Test
    void preservesSnapshotWithoutPolicyRelationshipInPersistenceRoundTrip() {
        Booking original = booking();
        original.registerPrivacyAcceptance(
                "2",
                "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "terms-v1"
        );

        BookingJpaEntity entity = BookingPersistenceMapper.toEntity(original);
        Booking restored = BookingPersistenceMapper.toDomain(entity);

        assertEquals(original.getPrivacyPolicyVersion(), restored.getPrivacyPolicyVersion());
        assertEquals(
                original.getPrivacyPolicyContentHash(),
                restored.getPrivacyPolicyContentHash()
        );
        assertEquals(original.getPrivacyAcceptedAt(), restored.getPrivacyAcceptedAt());
    }

    @Test
    void jpaEntityHasNoPolicyIdOrPolicyAssociation() {
        Set<String> fieldNameSet = Arrays.stream(BookingJpaEntity.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toSet());

        assertFalse(fieldNameSet.contains("privacyPolicyId"));
    }

    @Test
    void preservesLegacyAcceptanceWithoutInventingHash() {
        Booking legacyBooking = booking();
        legacyBooking.restorePersistenceState(
                9L,
                legacyBooking.getPaymentStatus(),
                "2026-legacy",
                null,
                "terms-v1",
                java.time.LocalDateTime.of(2026, 7, 1, 10, 0),
                false,
                null,
                java.time.LocalDateTime.of(2026, 7, 1, 9, 0),
                java.time.LocalDateTime.of(2026, 7, 1, 10, 0)
        );

        Booking restoredBooking = BookingPersistenceMapper.toDomain(
                BookingPersistenceMapper.toEntity(legacyBooking)
        );

        assertEquals("2026-legacy", restoredBooking.getPrivacyPolicyVersion());
        assertNull(restoredBooking.getPrivacyPolicyContentHash());
    }

    private Booking booking() {
        Guest guest = new Guest("Maria Silva", null, "+5512999999999", null, null, null);
        guest.restorePersistenceState(7L, null, List.of(), null, null);
        Room room = new Room(
                "Casa",
                RoomType.DOUBLE,
                4,
                new BigDecimal("350.00"),
                RoomStatus.AVAILABLE
        );
        room.restorePersistenceState(1L, null, null);
        return new Booking(
                guest,
                room,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                BookingStatus.UNCONFIRMED,
                new BigDecimal("700.00")
        );
    }
}
