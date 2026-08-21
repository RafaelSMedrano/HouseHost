package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionPlanJpaEntity;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface FinancialTransactionPlanJpaRepository
        extends JpaRepository<FinancialTransactionPlanJpaEntity, Long> {

    Optional<FinancialTransactionPlanJpaEntity> findBySourceTypeAndSourceId(
            FinancialTransactionSourceType sourceType,
            Long sourceId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select financialTransactionPlan
            from FinancialTransactionPlanJpaEntity financialTransactionPlan
            where financialTransactionPlan.sourceType = :sourceType
              and financialTransactionPlan.sourceId = :sourceId
            """)
    Optional<FinancialTransactionPlanJpaEntity> findBySourceForUpdate(
            @Param("sourceType") FinancialTransactionSourceType sourceType,
            @Param("sourceId") Long sourceId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select financialTransactionPlan
            from FinancialTransactionPlanJpaEntity financialTransactionPlan
            where financialTransactionPlan.id = :id
            """)
    Optional<FinancialTransactionPlanJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
