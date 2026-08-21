package com.househost.finance.financialtransaction.application.port.out;

import com.househost.finance.financialtransaction.application.records.FinancialCommandIdempotencyRecord;
import com.househost.finance.financialtransaction.application.records.FinancialCommandOperation;

import java.util.Optional;

public interface FinancialCommandIdempotencyPersistencePort {

    FinancialCommandIdempotencyRecord save(
            FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord
    );

    Optional<FinancialCommandIdempotencyRecord> find(
            FinancialCommandOperation operation,
            String actorReference,
            String idempotencyKey
    );
}
