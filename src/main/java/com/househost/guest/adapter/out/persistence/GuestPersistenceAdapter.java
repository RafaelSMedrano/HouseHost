package com.househost.guest.adapter.out.persistence;

import com.househost.guest.adapter.out.persistence.entity.GuestJpaEntity;
import com.househost.guest.adapter.out.persistence.entity.GuestPersistenceMapper;
import com.househost.guest.application.port.out.GuestPersistencePort;
import com.househost.guest.domain.model.Guest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GuestPersistenceAdapter implements GuestPersistencePort {
    private final GuestJpaRepository repository;

    public GuestPersistenceAdapter(GuestJpaRepository repository) {
        this.repository = repository;
    }

    public Guest save(Guest guest) { return GuestPersistenceMapper.toDomain(repository.save(GuestPersistenceMapper.toEntity(guest))); }
    public List<Guest> findAll() { return repository.findAll().stream().map(GuestPersistenceMapper::toDomain).toList(); }
    public Optional<Guest> findById(Long id) { return repository.findById(id).map(GuestPersistenceMapper::toDomain); }
    public Optional<Guest> findByEmail(String email) { return repository.findByEmail(email).map(GuestPersistenceMapper::toDomain); }
    public Optional<Guest> findByDocumentNumber(String value) { return repository.findByDocumentNumber(value).map(GuestPersistenceMapper::toDomain); }
    public List<Guest> findByFullNameIgnoreCase(String value) { return repository.findByFullNameIgnoreCase(value).stream().map(GuestPersistenceMapper::toDomain).toList(); }
    public List<Guest> findByFullNameContaining(String value) { return repository.findTop8ByFullNameContainingIgnoreCase(value).stream().map(GuestPersistenceMapper::toDomain).toList(); }
    public List<Guest> findByDocumentNumberContaining(String value) { return repository.findTop8ByDocumentNumberContainingIgnoreCase(value).stream().map(GuestPersistenceMapper::toDomain).toList(); }
    public List<Guest> findByEmailContaining(String value) { return repository.findTop20ByEmailContainingIgnoreCase(value).stream().map(GuestPersistenceMapper::toDomain).toList(); }
    public List<Guest> findByPhoneContaining(String value) { return repository.findTop20ByPhoneContainingIgnoreCase(value).stream().map(GuestPersistenceMapper::toDomain).toList(); }
    public List<Guest> findByCityContaining(String value) { return repository.findTop20ByCityContainingIgnoreCase(value).stream().map(GuestPersistenceMapper::toDomain).toList(); }
    public void delete(Guest guest) { repository.deleteById(guest.getId()); }
    public boolean existsByEmail(String value) { return repository.existsByEmail(value); }
    public boolean existsByEmailAndIdNot(String value, Long id) { return repository.existsByEmailAndIdNot(value, id); }
    public boolean existsByDocumentNumber(String value) { return repository.existsByDocumentNumber(value); }
    public boolean existsByDocumentNumberAndIdNot(String value, Long id) { return repository.existsByDocumentNumberAndIdNot(value, id); }
}
