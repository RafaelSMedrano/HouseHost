package com.househost.booking.booking.domain.model;

import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import com.househost.shared.exception.BookingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingPrivacyAcceptanceTest {
    @Test
    void storesIndependentImmutableSnapshotWithoutPolicyIdentifier() {
        Booking booking = booking();

        booking.registerPrivacyAcceptance(
                "2",
                "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "terms-v1"
        );

        assertEquals("2", booking.getPrivacyPolicyVersion());
        assertEquals(
                "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                booking.getPrivacyPolicyContentHash()
        );
        assertNotNull(booking.getPrivacyAcceptedAt());
        assertThrows(BookingException.class, () -> booking.registerPrivacyAcceptance(
                "3",
                "sha256:abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789",
                "terms-v1"
        ));
    }

    @Test
    void domainHasNoPrivacyPolicyIdentifierField() {
        Set<String> fieldNameSet = Arrays.stream(Booking.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toSet());

        assertFalse(fieldNameSet.contains("privacyPolicyId"));
    }

    @Test
    void rejectsAcceptanceWithoutServerIntegrityEvidence() {
        Booking booking = booking();

        assertThrows(
                BookingException.class,
                () -> booking.registerPrivacyAcceptance("2", "invalid", "terms-v1")
        );
    }

    private Booking booking() {
        Guest guest = new Guest("Maria Silva", null, "+5512999999999", null, null, null);
        Room room = new Room(
                "Casa",
                RoomType.DOUBLE,
                4,
                new BigDecimal("350.00"),
                RoomStatus.AVAILABLE
        );
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
