package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.adapter.out.persistence.entity.CashierJpaEntity;
import com.househost.finance.cashier.domain.model.Cashier;

final class CashierPersistenceMapper {
    private CashierPersistenceMapper() {
    }

    static Cashier toDomain(CashierJpaEntity entity) {
        Cashier cashier = new Cashier(
                entity.getName(),
                entity.getDescription(),
                entity.getOpeningBalance(),
                entity.getCashOnHand(),
                entity.getExpectedInflow(),
                entity.getExpectedOutflow(),
                entity.getTotalInflow(),
                entity.getTotalOutflow(),
                entity.getStatus()
        );
        cashier.restorePersistenceState(
                entity.getId(),
                entity.getOnWaiting(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
        return cashier;
    }

    static CashierJpaEntity toEntity(Cashier cashier) {
        return CashierJpaEntity.fromDomain(cashier);
    }
}
