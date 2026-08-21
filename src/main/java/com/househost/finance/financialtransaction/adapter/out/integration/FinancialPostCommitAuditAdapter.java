package com.househost.finance.financialtransaction.adapter.out.integration;

import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialPostCommitAuditPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

@Component
public class FinancialPostCommitAuditAdapter implements FinancialPostCommitAuditPort {

    private final FinancialAuditPort financialAuditPort;

    public FinancialPostCommitAuditAdapter(FinancialAuditPort financialAuditPort) {
        this.financialAuditPort = financialAuditPort;
    }

    @Override
    public void recordAfterCommit(
            String eventType,
            Long entityId,
            Map<String, Object> metadataMap
    ) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Auditoria pos-commit exige uma transacao financeira ativa."
            );
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        financialAuditPort.record(eventType, entityId, metadataMap);
                    }
                }
        );
    }
}
