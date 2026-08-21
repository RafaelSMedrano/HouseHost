package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialCommandIdempotencyJpaEntity;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandIdempotencyPersistencePort;
import com.househost.finance.financialtransaction.application.records.FinancialCommandIdempotencyRecord;
import com.househost.finance.financialtransaction.application.records.FinancialCommandOperation;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FinancialCommandIdempotencyPersistenceAdapter
        implements FinancialCommandIdempotencyPersistencePort {

    private final FinancialCommandIdempotencyJpaRepository financialCommandIdempotencyJpaRepository;

    public FinancialCommandIdempotencyPersistenceAdapter(
            FinancialCommandIdempotencyJpaRepository financialCommandIdempotencyJpaRepository
    ) {
        this.financialCommandIdempotencyJpaRepository = financialCommandIdempotencyJpaRepository;
    }

    @Override
    public FinancialCommandIdempotencyRecord save(
            FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord
    ) {
        FinancialCommandIdempotencyJpaEntity financialCommandIdempotencyJpaEntity =
                toEntity(financialCommandIdempotencyRecord);
        return toRecord(financialCommandIdempotencyJpaRepository.saveAndFlush(
                financialCommandIdempotencyJpaEntity
        ));
    }

    @Override
    public Optional<FinancialCommandIdempotencyRecord> find(
            FinancialCommandOperation operation,
            String actorReference,
            String idempotencyKey
    ) {
        return financialCommandIdempotencyJpaRepository
                .findByOperationAndActorReferenceAndIdempotencyKey(
                        operation,
                        actorReference,
                        idempotencyKey
                )
                .map(this::toRecord);
    }

    private FinancialCommandIdempotencyJpaEntity toEntity(
            FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord
    ) {
        FinancialCommandIdempotencyJpaEntity financialCommandIdempotencyJpaEntity =
                new FinancialCommandIdempotencyJpaEntity(
                        financialCommandIdempotencyRecord.operation(),
                        financialCommandIdempotencyRecord.actorReference(),
                        financialCommandIdempotencyRecord.idempotencyKey(),
                        financialCommandIdempotencyRecord.status(),
                        financialCommandIdempotencyRecord.bookingId(),
                        financialCommandIdempotencyRecord.planId(),
                        financialCommandIdempotencyRecord.financialTransactionId(),
                        financialCommandIdempotencyRecord.createdAt(),
                        financialCommandIdempotencyRecord.completedAt()
                );
        financialCommandIdempotencyJpaEntity.restoreId(
                financialCommandIdempotencyRecord.id()
        );
        return financialCommandIdempotencyJpaEntity;
    }

    private FinancialCommandIdempotencyRecord toRecord(
            FinancialCommandIdempotencyJpaEntity financialCommandIdempotencyJpaEntity
    ) {
        return new FinancialCommandIdempotencyRecord(
                financialCommandIdempotencyJpaEntity.getId(),
                financialCommandIdempotencyJpaEntity.getOperation(),
                financialCommandIdempotencyJpaEntity.getActorReference(),
                financialCommandIdempotencyJpaEntity.getIdempotencyKey(),
                financialCommandIdempotencyJpaEntity.getStatus(),
                financialCommandIdempotencyJpaEntity.getBookingId(),
                financialCommandIdempotencyJpaEntity.getPlanId(),
                financialCommandIdempotencyJpaEntity.getFinancialTransactionId(),
                financialCommandIdempotencyJpaEntity.getCreatedAt(),
                financialCommandIdempotencyJpaEntity.getCompletedAt()
        );
    }
}
