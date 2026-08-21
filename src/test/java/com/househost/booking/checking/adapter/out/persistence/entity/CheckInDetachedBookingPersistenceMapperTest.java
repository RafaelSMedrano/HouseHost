package com.househost.booking.checking.adapter.out.persistence.entity;

import com.househost.booking.checking.application.dto.CheckInResponseDTO;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckInDetachedBookingPersistenceMapperTest {

    @Test
    void preservesStayHistoryWhenBookingIsDetached() {
        CheckIn original = detachedCheckIn();

        CheckInJpaEntity entity = CheckInPersistenceMapper.toEntity(original);
        CheckIn restored = CheckInPersistenceMapper.toDomain(entity);
        CheckInResponseDTO response = new CheckInResponseDTO(restored);

        assertNull(entity.booking);
        assertNull(restored.getBooking());
        assertNull(response.getBookingId());
        assertEquals(7L, response.getGuestId());
        assertEquals(1L, response.getRoomId());
        assertEquals(CheckInStatus.COMPLETED.name(), response.getStatus());
    }

    @Test
    void mapsBookingAssociationAsOptionalWithoutRemoveCascade() throws Exception {
        var bookingField = CheckInJpaEntity.class.getDeclaredField("booking");
        OneToOne oneToOne = bookingField.getAnnotation(OneToOne.class);
        JoinColumn joinColumn = bookingField.getAnnotation(JoinColumn.class);

        assertTrue(oneToOne.optional());
        assertTrue(joinColumn.nullable());
        assertFalse(Arrays.asList(oneToOne.cascade()).contains(CascadeType.REMOVE));
    }

    private CheckIn detachedCheckIn() {
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

        CheckIn checkIn = new CheckIn(
                null,
                guest,
                room,
                2,
                0,
                0,
                true,
                true,
                true,
                true,
                true,
                null,
                null,
                "recepcao@househost.test",
                "Historico preservado",
                CheckInStatus.COMPLETED
        );
        checkIn.restorePersistenceState(11L, null);
        return checkIn;
    }
}
