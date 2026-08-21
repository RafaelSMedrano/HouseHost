package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.application.port.out.CashierPersistencePort;
import com.househost.finance.cashier.domain.model.Cashier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CashierPersistenceAdapter implements CashierPersistencePort {
    private final CashierJpaRepository cashierJpaRepository;

    public CashierPersistenceAdapter(CashierJpaRepository cashierJpaRepository) {
        this.cashierJpaRepository = cashierJpaRepository;
    }

    public Cashier save(Cashier cashier) {
        return CashierPersistenceMapper.toDomain(
                cashierJpaRepository.save(CashierPersistenceMapper.toEntity(cashier))
        );
    }

    public List<Cashier> findAll() {
        return cashierJpaRepository.findAll().stream()
                .map(CashierPersistenceMapper::toDomain)
                .toList();
    }

    public Optional<Cashier> findById(Long id) {
        return cashierJpaRepository.findById(id).map(CashierPersistenceMapper::toDomain);
    }

    public Optional<Cashier> findByIdForUpdate(Long id) {
        return cashierJpaRepository.findByIdForUpdate(id).map(CashierPersistenceMapper::toDomain);
    }

    public void delete(Cashier cashier) {
        cashierJpaRepository.deleteById(cashier.getId());
    }

    public boolean existsByName(String name) {
        return cashierJpaRepository.existsByName(name);
    }

    public boolean existsByNameAndIdNot(String name, Long id) {
        return cashierJpaRepository.existsByNameAndIdNot(name, id);
    }
}
