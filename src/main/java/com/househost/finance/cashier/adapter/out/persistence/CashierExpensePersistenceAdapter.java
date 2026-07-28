package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.adapter.out.persistence.entity.CashierExpenseJpaEntity;
import com.househost.finance.cashier.application.port.out.CashierExpensePersistencePort;
import com.househost.finance.cashier.domain.model.CashierExpense;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CashierExpensePersistenceAdapter implements CashierExpensePersistencePort {

    private final CashierExpenseJpaRepository repository;

    public CashierExpensePersistenceAdapter(CashierExpenseJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CashierExpense save(CashierExpense expense) {
        CashierExpenseJpaEntity saved = repository.save(CashierExpensePersistenceMapper.toEntity(expense));
        return CashierExpensePersistenceMapper.toDomain(saved);
    }

    @Override
    public List<CashierExpense> saveAll(Iterable<CashierExpense> expenses) {
        List<CashierExpenseJpaEntity> entities = new ArrayList<>();
        expenses.forEach(expense -> entities.add(CashierExpensePersistenceMapper.toEntity(expense)));
        return repository.saveAll(entities).stream()
                .map(CashierExpensePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<CashierExpense> findAll() {
        return repository.findAll().stream().map(CashierExpensePersistenceMapper::toDomain).toList();
    }

    @Override
    public Optional<CashierExpense> findById(Long id) {
        return repository.findById(id).map(CashierExpensePersistenceMapper::toDomain);
    }

    @Override
    public List<CashierExpense> findByCashierId(Long cashierId) {
        return repository.findByCashier_Id(cashierId).stream()
                .map(CashierExpensePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<CashierExpense> findBySourceTransactionId(Long sourceTransactionId) {
        return repository.findBySourceTransaction_Id(sourceTransactionId).stream()
                .map(CashierExpensePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CashierExpense> findBySourceTransactionIdAndCashierId(Long sourceTransactionId, Long cashierId) {
        return repository.findBySourceTransaction_IdAndCashier_Id(sourceTransactionId, cashierId)
                .map(CashierExpensePersistenceMapper::toDomain);
    }

    @Override
    public void delete(CashierExpense expense) {
        repository.deleteById(expense.getId());
    }

    @Override
    public void deleteAll(Iterable<? extends CashierExpense> expenses) {
        List<CashierExpenseJpaEntity> entities = new ArrayList<>();
        expenses.forEach(expense -> entities.add(CashierExpensePersistenceMapper.toEntity(expense)));
        repository.deleteAll(entities);
    }
}
