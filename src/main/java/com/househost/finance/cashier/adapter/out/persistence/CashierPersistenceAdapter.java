package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.application.port.out.CashierPersistencePort;
import com.househost.finance.cashier.domain.model.Cashier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CashierPersistenceAdapter implements CashierPersistencePort {
    private final CashierJpaRepository repository;

    public CashierPersistenceAdapter(CashierJpaRepository repository) {
        this.repository = repository;
    }

    public Cashier save(Cashier cashier) {
        return CashierPersistenceMapper.toDomain(repository.save(CashierPersistenceMapper.toEntity(cashier)));
    }

    public List<Cashier> findAll() {
        return repository.findAll().stream().map(CashierPersistenceMapper::toDomain).toList();
    }

    public Optional<Cashier> findById(Long id) {
        return repository.findById(id).map(CashierPersistenceMapper::toDomain);
    }

    public void delete(Cashier cashier) {
        repository.deleteById(cashier.getId());
    }

    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    public boolean existsByNameAndIdNot(String name, Long id) {
        return repository.existsByNameAndIdNot(name, id);
    }
}
