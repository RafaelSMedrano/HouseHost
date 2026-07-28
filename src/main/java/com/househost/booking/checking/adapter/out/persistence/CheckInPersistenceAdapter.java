package com.househost.booking.checking.adapter.out.persistence;

import com.househost.booking.checking.adapter.out.persistence.entity.CheckInPersistenceMapper;
import com.househost.booking.checking.application.port.out.CheckInPersistencePort;
import com.househost.booking.checking.domain.model.CheckIn;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class CheckInPersistenceAdapter implements CheckInPersistencePort {
    private final CheckInJpaRepository repository;
    public CheckInPersistenceAdapter(CheckInJpaRepository repository) { this.repository = repository; }
    public CheckIn save(CheckIn value) { return CheckInPersistenceMapper.toDomain(repository.save(CheckInPersistenceMapper.toEntity(value))); }
    public List<CheckIn> findAll() { return repository.findAll().stream().map(CheckInPersistenceMapper::toDomain).toList(); }
    public Optional<CheckIn> findById(Long id) { return repository.findById(id).map(CheckInPersistenceMapper::toDomain); }
    public Optional<CheckIn> findByBookingId(Long id) { return repository.findByBooking_Id(id).map(CheckInPersistenceMapper::toDomain); }
    public void delete(CheckIn value) { repository.deleteById(value.getId()); }
}
