package com.househost.ratings.adapter.out.persistence;

import com.househost.booking.booking.adapter.out.persistence.BookingPersistenceAdapter;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.adapter.out.persistence.GuestPersistenceAdapter;
import com.househost.guest.domain.model.Guest;
import com.househost.ratings.domain.model.Rating;
import com.househost.room.adapter.out.persistence.RoomPersistenceAdapter;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import({
        GuestPersistenceAdapter.class,
        RoomPersistenceAdapter.class,
        BookingPersistenceAdapter.class,
        RatingPersistenceAdapter.class
})
class RatingPersistenceConstraintTest {

    private final GuestPersistenceAdapter guestPersistenceAdapter;
    private final RoomPersistenceAdapter roomPersistenceAdapter;
    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final RatingPersistenceAdapter ratingPersistenceAdapter;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    RatingPersistenceConstraintTest(
            GuestPersistenceAdapter guestPersistenceAdapter,
            RoomPersistenceAdapter roomPersistenceAdapter,
            BookingPersistenceAdapter bookingPersistenceAdapter,
            RatingPersistenceAdapter ratingPersistenceAdapter,
            JdbcTemplate jdbcTemplate
    ) {
        this.guestPersistenceAdapter = guestPersistenceAdapter;
        this.roomPersistenceAdapter = roomPersistenceAdapter;
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
        this.ratingPersistenceAdapter = ratingPersistenceAdapter;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void databaseRejectsMoreThanOneRatingForTheSameBooking() {
        Booking booking = createPersistedBooking("101", "12345678901");
        ratingPersistenceAdapter.save(createRating(booking));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> ratingPersistenceAdapter.save(createRating(booking))
        );
    }

    @Test
    void databaseRejectsScoreOutsideAcceptedRange() {
        Booking booking = createPersistedBooking("102", "12345678902");
        Rating persistedRating = ratingPersistenceAdapter.save(createRating(booking));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "update ratings set comfort_score = 0 where id = ?",
                        persistedRating.getId()
                )
        );
    }

    @Test
    void databasePreventsRatedBookingFromCascadingAway() {
        Booking booking = createPersistedBooking("103", "12345678903");
        ratingPersistenceAdapter.save(createRating(booking));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "delete from bookings where id = ?",
                        booking.getId()
                )
        );

        assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "select count(*) from ratings where booking_id = ?",
                        Integer.class,
                        booking.getId()
                )
        );
    }

    private Booking createPersistedBooking(String roomNumber, String documentNumber) {
        Guest guest = guestPersistenceAdapter.save(
                new Guest("Hospede " + roomNumber, null, "11999999999", documentNumber)
        );
        Room room = roomPersistenceAdapter.save(
                new Room(
                        roomNumber,
                        RoomType.STANDARD,
                        2,
                        BigDecimal.valueOf(300),
                        RoomStatus.AVAILABLE
                )
        );
        return bookingPersistenceAdapter.save(
                new Booking(
                        guest,
                        room,
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 12),
                        BookingStatus.FINISHED,
                        BigDecimal.valueOf(600)
                )
        );
    }

    private Rating createRating(Booking booking) {
        return new Rating(
                booking,
                5,
                5,
                5,
                5,
                5,
                5,
                null,
                LocalDateTime.of(2026, 8, 12, 11, 30)
        );
    }
}
