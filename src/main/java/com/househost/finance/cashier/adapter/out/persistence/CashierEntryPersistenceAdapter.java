package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.adapter.out.persistence.entity.CashierEntryJpaEntity;
import com.househost.finance.cashier.application.port.out.CashierEntryPersistencePort;
import com.househost.finance.cashier.domain.model.CashierEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CashierEntryPersistenceAdapter implements CashierEntryPersistencePort {

    private final CashierEntryJpaRepository repository;

    public CashierEntryPersistenceAdapter(CashierEntryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CashierEntry save(CashierEntry entry) {
        CashierEntryJpaEntity saved = repository.save(CashierEntryPersistenceMapper.toEntity(entry));
        return CashierEntryPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<CashierEntry> saveAll(Iterable<CashierEntry> entries) {
        List<CashierEntryJpaEntity> entities = new ArrayList<>();
        entries.forEach(entry -> entities.add(CashierEntryPersistenceMapper.toEntity(entry)));
        return repository.saveAll(entities).stream()
                .map(CashierEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<CashierEntry> findAll() {
        return repository.findAll().stream().map(CashierEntryPersistenceMapper::toDomain).toList();
    }

    @Override
    public Optional<CashierEntry> findById(Long id) {
        return repository.findById(id).map(CashierEntryPersistenceMapper::toDomain);
    }

    @Override
    public List<CashierEntry> findByCashierId(Long cashierId) {
        return repository.findByCashier_Id(cashierId).stream()
                .map(CashierEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<CashierEntry> findBySourceTransactionId(Long sourceTransactionId) {
        return repository.findBySourceTransaction_Id(sourceTransactionId).stream()
                .map(CashierEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CashierEntry> findBySourceTransactionIdAndCashierId(Long sourceTransactionId, Long cashierId) {
        return repository.findBySourceTransaction_IdAndCashier_Id(sourceTransactionId, cashierId)
                .map(CashierEntryPersistenceMapper::toDomain);
    }

    @Override
    public void delete(CashierEntry entry) {
        repository.deleteById(entry.getId());
    }

    @Override
    public void deleteAll(Iterable<? extends CashierEntry> entries) {
        List<CashierEntryJpaEntity> entities = new ArrayList<>();
        entries.forEach(entry -> entities.add(CashierEntryPersistenceMapper.toEntity(entry)));
        repository.deleteAll(entities);
    }
}
