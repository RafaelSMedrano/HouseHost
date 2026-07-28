package com.househost.booking.checkout.adapter.out.persistence;

import com.househost.booking.checkout.adapter.out.persistence.entity.CheckOutPersistenceMapper;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.booking.checkout.domain.model.CheckOut;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CheckOutPersistenceAdapter implements CheckOutPersistencePort {
    private final CheckOutJpaRepository repository;

    public CheckOutPersistenceAdapter(CheckOutJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CheckOut save(CheckOut checkOut) {
        return CheckOutPersistenceMapper.toDomain(repository.save(CheckOutPersistenceMapper.toEntity(checkOut)));
    }

    @Override
    public List<CheckOut> findAll() {
        return repository.findAll().stream().map(CheckOutPersistenceMapper::toDomain).toList();
    }

    @Override
    public Optional<CheckOut> findById(Long id) {
        return repository.findById(id).map(CheckOutPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CheckOut> findByBookingId(Long bookingId) {
        return repository.findByBooking_Id(bookingId).map(CheckOutPersistenceMapper::toDomain);
    }

    @Override
    public void delete(CheckOut checkOut) {
        repository.deleteById(checkOut.getId());
    }
}
