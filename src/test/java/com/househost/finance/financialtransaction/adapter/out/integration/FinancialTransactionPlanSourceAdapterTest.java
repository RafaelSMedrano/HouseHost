package com.househost.finance.financialtransaction.adapter.out.integration;

import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanParticipationUseCase;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FinancialTransactionPlanSourceAdapterTest {

    private FinancialTransactionPlanParticipationUseCase financialTransactionPlanParticipationUseCase;
    private FinancialTransactionPlanSourceAdapter financialTransactionPlanSourceAdapter;

    @BeforeEach
    void setUp() {
        financialTransactionPlanParticipationUseCase = mock(
                FinancialTransactionPlanParticipationUseCase.class
        );
        financialTransactionPlanSourceAdapter = new FinancialTransactionPlanSourceAdapter(
                financialTransactionPlanParticipationUseCase
        );
    }

    @Test
    void exposesPlanSourceType() {
        assertEquals(
                FinancialTransactionSourceType.PLAN,
                financialTransactionPlanSourceAdapter.getType()
        );
    }

    @Test
    void delegatesCreationToTheNarrowAttachUseCase() {
        FinancialTransaction financialTransaction = mock(FinancialTransaction.class);

        financialTransactionPlanSourceAdapter.onCreate(50L, financialTransaction);

        verify(financialTransactionPlanParticipationUseCase).attach(50L, financialTransaction);
    }

    @Test
    void delegatesSettlementToTheNarrowRefreshUseCase() {
        FinancialTransaction financialTransaction = mock(FinancialTransaction.class);

        financialTransactionPlanSourceAdapter.onSettle(50L, financialTransaction);

        verify(financialTransactionPlanParticipationUseCase).refreshSettlement(50L);
    }

    @Test
    void delegatesDeletionToTheNarrowDetachUseCase() {
        FinancialTransaction financialTransaction = mock(FinancialTransaction.class);

        financialTransactionPlanSourceAdapter.onDelete(50L, financialTransaction);

        verify(financialTransactionPlanParticipationUseCase).detach(50L, financialTransaction);
    }
}
