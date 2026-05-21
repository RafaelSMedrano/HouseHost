package com.househost.finance.repository;

import com.househost.finance.model.CashierExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashierExpenseRepository extends JpaRepository<CashierExpense, Long> {

    List<CashierExpense> findByCashierId(Long cashierId);

    List<CashierExpense> findBySourceTransactionId(Long sourceTransactionId);
}
