package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.InstallmentPlanTransactionJpaEntity;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface InstallmentPlanTransactionJpaRepository
        extends JpaRepository<InstallmentPlanTransactionJpaEntity, Long> {

    @Query("""
            select distinct installmentPlanTransaction
            from InstallmentPlanTransactionJpaEntity installmentPlanTransaction
            left join fetch installmentPlanTransaction.installmentTransactionJpaEntityList
            where installmentPlanTransaction.sourceType = :sourceType
              and installmentPlanTransaction.sourceId = :planId
            """)
    List<InstallmentPlanTransactionJpaEntity> findDirectPlanComponentListWithInstallments(
            @Param("sourceType") FinancialTransactionSourceType sourceType,
            @Param("planId") Long planId
    );
}
