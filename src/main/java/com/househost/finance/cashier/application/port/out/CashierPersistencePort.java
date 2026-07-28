package com.househost.finance.cashier.application.port.out;

import com.househost.finance.cashier.domain.model.Cashier;

import java.util.List;
import java.util.Optional;

public interface CashierPersistencePort {
    Cashier save(Cashier cashier);
    List<Cashier> findAll();
    Optional<Cashier> findById(Long id);
    void delete(Cashier cashier);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
