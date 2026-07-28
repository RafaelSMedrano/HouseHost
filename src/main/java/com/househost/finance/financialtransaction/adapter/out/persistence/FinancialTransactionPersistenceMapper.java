package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionJpaEntity;
import com.househost.finance.financialtransaction.adapter.out.persistence.entity.InstallmentPlanTransactionJpaEntity;
import com.househost.finance.financialtransaction.adapter.out.persistence.entity.InstallmentTransactionJpaEntity;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransaction;

import java.util.List;

final class FinancialTransactionPersistenceMapper {

    private FinancialTransactionPersistenceMapper() {
    }

    static FinancialTransactionJpaEntity toEntity(FinancialTransaction transaction) {
        if (transaction instanceof InstallmentPlanTransaction plan) {
            return toPlanEntity(plan);
        }
        if (transaction instanceof InstallmentTransaction installment) {
            InstallmentPlanTransactionJpaEntity planReference = new InstallmentPlanTransactionJpaEntity();
            planReference.restorePersistenceState(installment.getInstallmentPlan().getId(), null, null, null, null);
            return toInstallmentEntity(installment, planReference);
        }

        FinancialTransactionJpaEntity entity = new FinancialTransactionJpaEntity(
                transaction.getSenderType(),
                transaction.getSenderId(),
                transaction.getReceiverType(),
                transaction.getReceiverId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getDueDate(),
                transaction.getDescription(),
                transaction.getMethod(),
                transaction.getStatus()
        );
        restoreCommonEntityState(transaction, entity);
        return entity;
    }

    static FinancialTransaction toDomain(FinancialTransactionJpaEntity entity) {
        if (entity instanceof InstallmentPlanTransactionJpaEntity planEntity) {
            return toPlanDomain(planEntity);
        }
        if (entity instanceof InstallmentTransactionJpaEntity installmentEntity) {
            InstallmentPlanTransaction planReference = new InstallmentPlanTransaction();
            planReference.restorePersistenceState(
                    installmentEntity.getInstallmentPlan().getId(),
                    installmentEntity.getInstallmentPlan().getCreationDate(),
                    installmentEntity.getInstallmentPlan().getSettlementDate(),
                    installmentEntity.getInstallmentPlan().getCreatedAt(),
                    installmentEntity.getInstallmentPlan().getUpdatedAt()
            );
            return toInstallmentDomain(installmentEntity, planReference);
        }

        FinancialTransaction transaction = new FinancialTransaction(
                entity.getSenderType(),
                entity.getSenderId(),
                entity.getReceiverType(),
                entity.getReceiverId(),
                entity.getType(),
                entity.getAmount(),
                entity.getTransactionDate(),
                entity.getDueDate(),
                entity.getDescription(),
                entity.getMethod(),
                entity.getStatus()
        );
        restoreCommonDomainState(entity, transaction);
        return transaction;
    }

    private static InstallmentPlanTransactionJpaEntity toPlanEntity(InstallmentPlanTransaction plan) {
        InstallmentPlanTransactionJpaEntity entity = new InstallmentPlanTransactionJpaEntity(
                plan.getSenderType(),
                plan.getSenderId(),
                plan.getReceiverType(),
                plan.getReceiverId(),
                plan.getType(),
                plan.getAmount(),
                plan.getTransactionDate(),
                plan.getDescription(),
                plan.getMethod(),
                plan.getInstallmentsQuantity(),
                plan.getInstallmentDueDay(),
                plan.getStatus()
        );
        restoreCommonEntityState(plan, entity);

        List<InstallmentTransactionJpaEntity> installments = plan.getInstallments().stream()
                .map(installment -> toInstallmentEntity(installment, entity))
                .toList();
        entity.replaceInstallments(installments);
        return entity;
    }

    private static InstallmentTransactionJpaEntity toInstallmentEntity(
            InstallmentTransaction installment,
            InstallmentPlanTransactionJpaEntity planEntity
    ) {
        InstallmentTransactionJpaEntity entity = new InstallmentTransactionJpaEntity(
                installment.getSenderType(),
                installment.getSenderId(),
                installment.getReceiverType(),
                installment.getReceiverId(),
                installment.getType(),
                installment.getAmount(),
                installment.getTransactionDate(),
                installment.getDescription(),
                installment.getMethod(),
                planEntity,
                installment.getInstallmentNumber(),
                installment.getTotalInstallments(),
                installment.getDueDate(),
                installment.getInstallmentStatus()
        );
        entity.setStatus(installment.getStatus());
        restoreCommonEntityState(installment, entity);
        return entity;
    }

    private static InstallmentPlanTransaction toPlanDomain(InstallmentPlanTransactionJpaEntity entity) {
        InstallmentPlanTransaction plan = new InstallmentPlanTransaction(
                entity.getSenderType(),
                entity.getSenderId(),
                entity.getReceiverType(),
                entity.getReceiverId(),
                entity.getType(),
                entity.getAmount(),
                entity.getTransactionDate(),
                entity.getDescription(),
                entity.getMethod(),
                entity.getInstallmentsQuantity(),
                entity.getInstallmentDueDay(),
                entity.getStatus()
        );
        restoreCommonDomainState(entity, plan);

        List<InstallmentTransaction> installments = entity.getInstallments().stream()
                .map(installment -> toInstallmentDomain(installment, plan))
                .toList();
        plan.restoreInstallments(installments);
        return plan;
    }

    private static InstallmentTransaction toInstallmentDomain(
            InstallmentTransactionJpaEntity entity,
            InstallmentPlanTransaction plan
    ) {
        InstallmentTransaction installment = InstallmentTransaction.restore(
                entity.getSenderType(),
                entity.getSenderId(),
                entity.getReceiverType(),
                entity.getReceiverId(),
                entity.getType(),
                entity.getAmount(),
                entity.getTransactionDate(),
                entity.getDescription(),
                entity.getMethod(),
                plan,
                entity.getInstallmentNumber(),
                entity.getTotalInstallments(),
                entity.getDueDate(),
                entity.getInstallmentStatus()
        );
        installment.setStatus(entity.getStatus());
        restoreCommonDomainState(entity, installment);
        return installment;
    }

    private static void restoreCommonEntityState(
            FinancialTransaction transaction,
            FinancialTransactionJpaEntity entity
    ) {
        entity.setSource(transaction.getSourceType(), transaction.getSourceId());
        entity.restorePersistenceState(
                transaction.getId(),
                transaction.getCreationDate(),
                transaction.getSettlementDate(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    private static void restoreCommonDomainState(
            FinancialTransactionJpaEntity entity,
            FinancialTransaction transaction
    ) {
        transaction.setSource(entity.getSourceType(), entity.getSourceId());
        transaction.restorePersistenceState(
                entity.getId(),
                entity.getCreationDate(),
                entity.getSettlementDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
