package com.househost.finance.cashier.application.port.out;

import com.househost.finance.cashier.domain.model.CashierExpense;

import java.util.List;
import java.util.Optional;

public interface CashierExpensePersistencePort {
    CashierExpense save(CashierExpense expense);
    List<CashierExpense> saveAll(Iterable<CashierExpense> expenses);
    List<CashierExpense> findAll();
    Optional<CashierExpense> findById(Long id);
    List<CashierExpense> findByCashierId(Long cashierId);
    List<CashierExpense> findBySourceTransactionId(Long sourceTransactionId);
    Optional<CashierExpense> findBySourceTransactionIdAndCashierId(Long sourceTransactionId, Long cashierId);
    void delete(CashierExpense expense);
    void deleteAll(Iterable<? extends CashierExpense> expenses);
}
