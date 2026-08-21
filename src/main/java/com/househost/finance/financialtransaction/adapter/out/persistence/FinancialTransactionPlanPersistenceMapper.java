package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionPlanJpaEntity;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;

import java.util.List;

final class FinancialTransactionPlanPersistenceMapper {

    private FinancialTransactionPlanPersistenceMapper() {
    }

    static FinancialTransactionPlanJpaEntity toEntity(
            FinancialTransactionPlan financialTransactionPlan
    ) {
        FinancialTransactionPlanJpaEntity financialTransactionPlanJpaEntity =
                new FinancialTransactionPlanJpaEntity(
                        financialTransactionPlan.getSenderType(),
                        financialTransactionPlan.getSenderId(),
                        financialTransactionPlan.getReceiverType(),
                        financialTransactionPlan.getReceiverId(),
                        financialTransactionPlan.getSourceType(),
                        financialTransactionPlan.getSourceId(),
                        financialTransactionPlan.getTotalAmount(),
                        financialTransactionPlan.getStatus(),
                        financialTransactionPlan.getPlanDueDate(),
                        financialTransactionPlan.getPlanSettlementDate(),
                        financialTransactionPlan.getDescription()
                );
        financialTransactionPlanJpaEntity.restorePersistenceState(
                financialTransactionPlan.getId(),
                financialTransactionPlan.getVersion(),
                financialTransactionPlan.getCreatedAt(),
                financialTransactionPlan.getUpdatedAt()
        );
        return financialTransactionPlanJpaEntity;
    }

    static FinancialTransactionPlan toDomain(
            FinancialTransactionPlanJpaEntity financialTransactionPlanJpaEntity,
            List<FinancialTransaction> financialTransactionList
    ) {
        FinancialTransactionPlan financialTransactionPlan = new FinancialTransactionPlan(
                financialTransactionPlanJpaEntity.getId(),
                financialTransactionPlanJpaEntity.getSenderType(),
                financialTransactionPlanJpaEntity.getSenderId(),
                financialTransactionPlanJpaEntity.getReceiverType(),
                financialTransactionPlanJpaEntity.getReceiverId(),
                financialTransactionPlanJpaEntity.getSourceType(),
                financialTransactionPlanJpaEntity.getSourceId(),
                financialTransactionList,
                financialTransactionPlanJpaEntity.getPlanDueDate(),
                financialTransactionPlanJpaEntity.getDescription()
        );
        financialTransactionPlan.restorePersistenceState(
                financialTransactionPlanJpaEntity.getVersion(),
                financialTransactionPlanJpaEntity.getStatus(),
                financialTransactionPlanJpaEntity.getCreatedAt(),
                financialTransactionPlanJpaEntity.getUpdatedAt()
        );
        if (financialTransactionPlan.getTotalAmount().compareTo(
                financialTransactionPlanJpaEntity.getTotalAmount()
        ) != 0) {
            throw new IllegalStateException("Total persistido do plano financeiro e inconsistente.");
        }
        return financialTransactionPlan;
    }
}
