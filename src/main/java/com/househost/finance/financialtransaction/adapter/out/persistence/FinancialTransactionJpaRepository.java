package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface FinancialTransactionJpaRepository extends JpaRepository<FinancialTransactionJpaEntity, Long> {
}
