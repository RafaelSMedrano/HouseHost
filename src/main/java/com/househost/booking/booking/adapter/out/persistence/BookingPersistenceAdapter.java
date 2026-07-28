package com.househost.booking.booking.adapter.out.persistence;

import com.househost.booking.booking.adapter.out.persistence.entity.BookingPersistenceMapper;
import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class BookingPersistenceAdapter implements BookingPersistencePort {

    private final BookingJpaRepository repository;

    public BookingPersistenceAdapter(BookingJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Booking save(Booking booking) {
        return BookingPersistenceMapper.toDomain(repository.save(BookingPersistenceMapper.toEntity(booking)));
    }

    @Override
    public List<Booking> findAll() {
        return repository.findAll().stream().map(BookingPersistenceMapper::toDomain).toList();
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return repository.findById(id).map(BookingPersistenceMapper::toDomain);
    }

    @Override
    public void delete(Booking booking) {
        repository.deleteById(booking.getId());
    }

    @Override
    public long countByStatus(BookingStatus status) {
        return repository.countByStatus(status);
    }

    @Override
    public List<Booking> findByGuestId(Long guestId) {
        return repository.findByGuestId(guestId).stream().map(BookingPersistenceMapper::toDomain).toList();
    }

    @Override
    public List<Booking> findByGuestIdIn(Collection<Long> guestIds) {
        return repository.findByGuestIdIn(guestIds).stream().map(BookingPersistenceMapper::toDomain).toList();
    }

    @Override
    public List<Booking> findByRoomId(Long roomId) {
        return repository.findByRoomId(roomId).stream().map(BookingPersistenceMapper::toDomain).toList();
    }

    @Override
    public List<Booking> findOverlappingBookings(
            Long roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Collection<BookingStatus> statuses
    ) {
        return repository.findOverlappingBookings(roomId, checkInDate, checkOutDate, statuses)
                .stream()
                .map(BookingPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsOverlappingBooking(
            Long roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Collection<BookingStatus> statuses
    ) {
        return repository.existsOverlappingBooking(roomId, checkInDate, checkOutDate, statuses);
    }

    @Override
    public boolean existsOverlappingBookingIgnoringId(
            Long roomId,
            Long bookingId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Collection<BookingStatus> statuses
    ) {
        return repository.existsOverlappingBookingIgnoringId(
                roomId, bookingId, checkInDate, checkOutDate, statuses
        );
    }
}
