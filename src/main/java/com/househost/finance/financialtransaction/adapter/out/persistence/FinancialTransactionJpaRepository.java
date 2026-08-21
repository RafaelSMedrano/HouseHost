package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionJpaEntity;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface FinancialTransactionJpaRepository extends JpaRepository<FinancialTransactionJpaEntity, Long> {

    @Query("""
            select financialTransaction
            from FinancialTransactionJpaEntity financialTransaction
            where financialTransaction.sourceType = :sourceType
              and financialTransaction.sourceId = :planId
            order by financialTransaction.dueDate,
                     financialTransaction.planComponentOrder
            """)
    List<FinancialTransactionJpaEntity> findDirectPlanComponentList(
            @Param("sourceType") FinancialTransactionSourceType sourceType,
            @Param("planId") Long planId
    );
}
