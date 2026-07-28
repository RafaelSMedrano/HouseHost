package com.househost.finance.financialtransaction.application.port.out;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;

import java.util.List;
import java.util.Optional;

public interface FinancialTransactionPersistencePort {
    <S extends FinancialTransaction> S save(S transaction);
    List<FinancialTransaction> findAll();
    Optional<FinancialTransaction> findById(Long id);
    void delete(FinancialTransaction transaction);
}
