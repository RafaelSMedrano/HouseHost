package com.househost.finance.financialtransaction.adapter.out.integration;

import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class FinancialPostCommitAuditAdapterTest {

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void recordsOnlyAfterTheFinancialTransactionCommits() {
        FinancialAuditPort financialAuditPort = mock(FinancialAuditPort.class);
        FinancialPostCommitAuditAdapter financialPostCommitAuditAdapter =
                new FinancialPostCommitAuditAdapter(financialAuditPort);
        Map<String, Object> metadataMap = Map.of(
                "type",
                "PLAN_CHECK_IN_PAYMENT"
        );
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();

        financialPostCommitAuditAdapter.recordAfterCommit(
                "FINANCIAL_TRANSACTION_CREATED",
                201L,
                metadataMap
        );

        verifyNoInteractions(financialAuditPort);
        List<TransactionSynchronization> transactionSynchronizationList =
                TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, transactionSynchronizationList.size());
        transactionSynchronizationList.get(0).afterCommit();
        verify(financialAuditPort).record(
                "FINANCIAL_TRANSACTION_CREATED",
                201L,
                metadataMap
        );
    }

    @Test
    void rejectsSchedulingOutsideAnActiveTransaction() {
        FinancialAuditPort financialAuditPort = mock(FinancialAuditPort.class);
        FinancialPostCommitAuditAdapter financialPostCommitAuditAdapter =
                new FinancialPostCommitAuditAdapter(financialAuditPort);

        assertThrows(
                IllegalStateException.class,
                () -> financialPostCommitAuditAdapter.recordAfterCommit(
                        "FINANCIAL_TRANSACTION_CREATED",
                        201L,
                        Map.of()
                )
        );

        verifyNoInteractions(financialAuditPort);
    }
}
