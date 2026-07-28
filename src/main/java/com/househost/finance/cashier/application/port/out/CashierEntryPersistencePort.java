package com.househost.finance.cashier.application.port.out;

import com.househost.finance.cashier.domain.model.CashierEntry;

import java.util.List;
import java.util.Optional;

public interface CashierEntryPersistencePort {
    CashierEntry save(CashierEntry entry);
    List<CashierEntry> saveAll(Iterable<CashierEntry> entries);
    List<CashierEntry> findAll();
    Optional<CashierEntry> findById(Long id);
    List<CashierEntry> findByCashierId(Long cashierId);
    List<CashierEntry> findBySourceTransactionId(Long sourceTransactionId);
    Optional<CashierEntry> findBySourceTransactionIdAndCashierId(Long sourceTransactionId, Long cashierId);
    void delete(CashierEntry entry);
    void deleteAll(Iterable<? extends CashierEntry> entries);
}
