package com.househost.ratings.adapter.out.persistence;

import com.househost.booking.booking.adapter.out.persistence.BookingPersistenceAdapter;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.adapter.out.persistence.GuestPersistenceAdapter;
import com.househost.guest.domain.model.Guest;
import com.househost.ratings.application.records.RatingPageRecord;
import com.househost.ratings.application.dto.RatingPageResponseDTO;
import com.househost.ratings.domain.model.Rating;
import com.househost.room.adapter.out.persistence.RoomPersistenceAdapter;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({
        GuestPersistenceAdapter.class,
        RoomPersistenceAdapter.class,
        BookingPersistenceAdapter.class,
        RatingPersistenceAdapter.class
})
class RatingPersistencePaginationTest {

    private final GuestPersistenceAdapter guestPersistenceAdapter;
    private final RoomPersistenceAdapter roomPersistenceAdapter;
    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final RatingPersistenceAdapter ratingPersistenceAdapter;
    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    RatingPersistencePaginationTest(
            GuestPersistenceAdapter guestPersistenceAdapter,
            RoomPersistenceAdapter roomPersistenceAdapter,
            BookingPersistenceAdapter bookingPersistenceAdapter,
            RatingPersistenceAdapter ratingPersistenceAdapter,
            EntityManagerFactory entityManagerFactory
    ) {
        this.guestPersistenceAdapter = guestPersistenceAdapter;
        this.roomPersistenceAdapter = roomPersistenceAdapter;
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
        this.ratingPersistenceAdapter = ratingPersistenceAdapter;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Test
    void returnsBoundedDeterministicGeneralAndGuestPages() {
        Guest firstGuest = createGuest("Primeiro hospede", "12345678901");
        Guest secondGuest = createGuest("Segundo hospede", "12345678902");
        Rating firstRating = createRating(
                createBooking(firstGuest, "201"),
                LocalDateTime.of(2026, 8, 12, 10, 0)
        );
        Rating secondRating = createRating(
                createBooking(firstGuest, "202"),
                LocalDateTime.of(2026, 8, 12, 11, 0)
        );
        Rating thirdRating = createRating(
                createBooking(secondGuest, "203"),
                LocalDateTime.of(2026, 8, 12, 11, 0)
        );

        RatingPageRecord firstPageRecord = ratingPersistenceAdapter.findAll(0, 2);
        RatingPageRecord secondPageRecord = ratingPersistenceAdapter.findAll(1, 2);
        RatingPageRecord guestPageRecord = ratingPersistenceAdapter.findByGuestId(
                firstGuest.getId(),
                0,
                10
        );

        assertEquals(3, firstPageRecord.totalElements());
        assertEquals(2, firstPageRecord.totalPages());
        assertEquals(
                thirdRating.getBooking().getId(),
                firstPageRecord.ratingSummaryRecordList().get(0).bookingId()
        );
        assertEquals(
                secondRating.getBooking().getId(),
                firstPageRecord.ratingSummaryRecordList().get(1).bookingId()
        );
        assertEquals(
                firstRating.getBooking().getId(),
                secondPageRecord.ratingSummaryRecordList().getFirst().bookingId()
        );
        assertEquals(2, guestPageRecord.totalElements());
        assertEquals(
                secondRating.getBooking().getId(),
                guestPageRecord.ratingSummaryRecordList().get(0).bookingId()
        );
        assertEquals(
                firstRating.getBooking().getId(),
                guestPageRecord.ratingSummaryRecordList().get(1).bookingId()
        );
    }

    @Test
    void materializesEachPageWithBoundedQueriesAndNoLazyRowLoading() {
        Guest guest = createGuest("Hospede consultado", "12345678903");
        createRating(createBooking(guest, "204"), LocalDateTime.of(2026, 8, 12, 10, 0));
        createRating(createBooking(guest, "205"), LocalDateTime.of(2026, 8, 12, 11, 0));
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        sessionFactory.getStatistics().setStatisticsEnabled(true);
        sessionFactory.getStatistics().clear();

        RatingPageRecord ratingPageRecord = ratingPersistenceAdapter.findByGuestId(
                guest.getId(),
                0,
                20
        );
        RatingPageResponseDTO ratingPageResponseDTO = new RatingPageResponseDTO(
                ratingPageRecord
        );

        long preparedStatementCount = sessionFactory.getStatistics()
                .getPrepareStatementCount();
        assertEquals(2, ratingPageResponseDTO.getRatingSummaryDTOList().size());
        assertTrue(preparedStatementCount > 0);
        assertTrue(preparedStatementCount <= 2);
    }

    @Test
    void missingGuestHasEmptyBoundedHistory() {
        RatingPageRecord ratingPageRecord = ratingPersistenceAdapter.findByGuestId(
                999L,
                0,
                20
        );

        assertEquals(0, ratingPageRecord.totalElements());
        assertEquals(0, ratingPageRecord.ratingSummaryRecordList().size());
    }

    private Guest createGuest(String fullName, String documentNumber) {
        return guestPersistenceAdapter.save(
                new Guest(fullName, null, "11999999999", documentNumber)
        );
    }

    private Booking createBooking(Guest guest, String roomNumber) {
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

    private Rating createRating(Booking booking, LocalDateTime evaluatedAt) {
        return ratingPersistenceAdapter.save(
                new Rating(booking, 5, 4, 5, 4, 5, 4, null, evaluatedAt)
        );
    }
}
