package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.adapter.out.persistence.entity.CashierEntryJpaEntity;
import com.househost.finance.cashier.domain.model.CashierEntry;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;

final class CashierEntryPersistenceMapper {

    private CashierEntryPersistenceMapper() {
    }

    static CashierEntry toDomain(CashierEntryJpaEntity entity) {
        FinancialTransaction sourceTransaction = entity.getSourceTransaction() == null
                ? null
                : FinancialTransaction.reference(entity.getSourceTransaction().getId());
        CashierEntry entry = new CashierEntry(
                CashierPersistenceMapper.toDomain(entity.getCashier()),
                entity.getDescription(),
                entity.getAmount(),
                entity.getDueDate(),
                entity.getSettlementDate(),
                entity.getSource(),
                entity.getStatus(),
                sourceTransaction
        );
        entry.restorePersistenceState(entity.getId(), entity.getCreatedAt(), entity.getUpdatedAt());
        return entry;
    }

    static CashierEntryJpaEntity toEntity(CashierEntry entry) {
        return CashierEntryJpaEntity.fromDomain(entry);
    }
}
