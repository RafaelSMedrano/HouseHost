package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialCommandIdempotencyJpaEntity;
import com.househost.finance.financialtransaction.application.records.FinancialCommandOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface FinancialCommandIdempotencyJpaRepository
        extends JpaRepository<FinancialCommandIdempotencyJpaEntity, Long> {

    Optional<FinancialCommandIdempotencyJpaEntity> findByOperationAndActorReferenceAndIdempotencyKey(
            FinancialCommandOperation operation,
            String actorReference,
            String idempotencyKey
    );
}
