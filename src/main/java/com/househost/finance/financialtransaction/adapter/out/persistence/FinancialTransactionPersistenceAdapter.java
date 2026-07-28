package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionJpaEntity;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPersistencePort;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FinancialTransactionPersistenceAdapter implements FinancialTransactionPersistencePort {

    private final FinancialTransactionJpaRepository repository;

    public FinancialTransactionPersistenceAdapter(FinancialTransactionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public <S extends FinancialTransaction> S save(S transaction) {
        FinancialTransactionJpaEntity saved = repository.save(
                FinancialTransactionPersistenceMapper.toEntity(transaction)
        );
        return cast(FinancialTransactionPersistenceMapper.toDomain(saved));
    }

    @Override
    public List<FinancialTransaction> findAll() {
        return repository.findAll().stream()
                .map(FinancialTransactionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<FinancialTransaction> findById(Long id) {
        return repository.findById(id).map(FinancialTransactionPersistenceMapper::toDomain);
    }

    @Override
    public void delete(FinancialTransaction transaction) {
        repository.deleteById(transaction.getId());
    }

    @SuppressWarnings("unchecked")
    private <S extends FinancialTransaction> S cast(FinancialTransaction transaction) {
        return (S) transaction;
    }
}
