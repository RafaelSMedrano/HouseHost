package com.househost.booking.checkout.adapter.out.persistence;

import com.househost.booking.checkout.adapter.out.persistence.entity.CheckOutPersistenceMapper;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.booking.checkout.domain.model.CheckOut;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CheckOutPersistenceAdapter implements CheckOutPersistencePort {
    private final CheckOutJpaRepository checkOutRepository;

    public CheckOutPersistenceAdapter(CheckOutJpaRepository checkOutRepository) {
        this.checkOutRepository = checkOutRepository;
    }

    @Override
    public CheckOut save(CheckOut checkOut) {
        return CheckOutPersistenceMapper.toDomain(
                checkOutRepository.save(CheckOutPersistenceMapper.toEntity(checkOut))
        );
    }

    @Override
    public List<CheckOut> findAll() {
        return checkOutRepository.findAll()
                .stream()
                .map(CheckOutPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CheckOut> findById(Long id) {
        return checkOutRepository.findById(id).map(CheckOutPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CheckOut> findByIdForUpdate(Long id) {
        return checkOutRepository.findByIdForUpdate(id).map(CheckOutPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CheckOut> findByBookingId(Long bookingId) {
        return checkOutRepository.findByBooking_Id(bookingId)
                .map(CheckOutPersistenceMapper::toDomain);
    }

    @Override
    public void delete(CheckOut checkOut) {
        checkOutRepository.deleteById(checkOut.getId());
    }
}
