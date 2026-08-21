package com.househost.booking.checkout.adapter.out.persistence.entity;

import com.househost.booking.checkout.application.dto.CheckOutResponseDTO;
import com.househost.booking.checkout.adapter.out.persistence.CheckOutJpaRepository;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckOutDetachedBookingPersistenceMapperTest {

    @Test
    void preservesStayHistoryWhenBookingIsDetached() {
        CheckOut original = detachedCheckOut();

        CheckOutJpaEntity entity = CheckOutPersistenceMapper.toEntity(original);
        CheckOut restored = CheckOutPersistenceMapper.toDomain(entity);
        CheckOutResponseDTO response = new CheckOutResponseDTO(restored);

        assertNull(entity.booking);
        assertNull(restored.getBooking());
        assertNull(response.getBookingId());
        assertEquals(7L, response.getGuestId());
        assertEquals(1L, response.getRoomId());
        assertEquals(CheckOutStatus.COMPLETED, response.getStatus());
        assertTrue(restored.isGuestHistoryApplied());
    }

    @Test
    void mapsBookingAssociationAsOptionalWithoutRemoveCascade() throws Exception {
        var bookingField = CheckOutJpaEntity.class.getDeclaredField("booking");
        OneToOne oneToOne = bookingField.getAnnotation(OneToOne.class);
        JoinColumn joinColumn = bookingField.getAnnotation(JoinColumn.class);

        assertTrue(oneToOne.optional());
        assertTrue(joinColumn.nullable());
        assertFalse(Arrays.asList(oneToOne.cascade()).contains(CascadeType.REMOVE));
    }

    @Test
    void persistsHistoryEvidenceAndLocksCheckoutDuringUpdate() throws Exception {
        var guestHistoryAppliedField = CheckOutJpaEntity.class
                .getDeclaredField("guestHistoryApplied");
        Column column = guestHistoryAppliedField.getAnnotation(Column.class);
        Lock lock = CheckOutJpaRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);

        assertFalse(column.nullable());
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
    }

    private CheckOut detachedCheckOut() {
        Guest guest = new Guest("Roberto Jr", null, "+5512999999999", null, null, null);
        guest.restorePersistenceState(7L, null, List.of(), null, null);
        Room room = new Room(
                "101",
                RoomType.DOUBLE,
                2,
                new BigDecimal("350.00"),
                RoomStatus.AVAILABLE
        );
        room.restorePersistenceState(1L, null, null);

        CheckOut checkOut = new CheckOut(
                null,
                guest,
                room,
                LocalDateTime.of(2026, 8, 10, 11, 0),
                true,
                true,
                true,
                true,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "recepcao@househost.test",
                "Historico preservado",
                CheckOutStatus.COMPLETED
        );
        checkOut.restorePersistenceState(12L, true, null, null);
        return checkOut;
    }
}
