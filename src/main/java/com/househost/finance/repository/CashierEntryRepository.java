package com.househost.finance.repository;

import com.househost.finance.model.CashierEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashierEntryRepository extends JpaRepository<CashierEntry, Long> {

    List<CashierEntry> findByCashierId(Long cashierId);

    List<CashierEntry> findBySourceTransactionId(Long sourceTransactionId);
}
