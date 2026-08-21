package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.adapter.out.persistence.entity.CashierExpenseJpaEntity;
import com.househost.finance.cashier.domain.model.CashierExpense;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;

final class CashierExpensePersistenceMapper {

    private CashierExpensePersistenceMapper() {
    }

    static CashierExpense toDomain(CashierExpenseJpaEntity entity) {
        FinancialTransaction sourceTransaction = entity.getSourceTransaction() == null
                ? null
                : FinancialTransaction.reference(entity.getSourceTransaction().getId());
        CashierExpense expense = new CashierExpense(
                CashierPersistenceMapper.toDomain(entity.getCashier()),
                entity.getDescription(),
                entity.getAmount(),
                entity.getDueDate(),
                entity.getSettlementDate(),
                entity.getCategory(),
                entity.getStatus(),
                sourceTransaction
        );
        expense.restorePersistenceState(entity.getId(), entity.getCreatedAt(), entity.getUpdatedAt());
        return expense;
    }

    static CashierExpenseJpaEntity toEntity(CashierExpense expense) {
        return CashierExpenseJpaEntity.fromDomain(expense);
    }
}
