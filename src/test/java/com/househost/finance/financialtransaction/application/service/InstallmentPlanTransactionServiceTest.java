package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPersistencePort;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InstallmentPlanTransactionServiceTest {

    @Test
    void auditsInstallmentPlanAfterPersistenceAndParticipantNotification() {
        FinancialTransactionPersistencePort financialTransactionPersistencePort =
                mock(FinancialTransactionPersistencePort.class);
        InstallmentTransactionService installmentTransactionService = mock(InstallmentTransactionService.class);
        FinancialParticipantNotifier financialParticipantNotifier = mock(FinancialParticipantNotifier.class);
        FinancialAuditPort financialAuditPort = mock(FinancialAuditPort.class);
        InstallmentPlanTransactionService installmentPlanTransactionService =
                new InstallmentPlanTransactionService(
                        financialTransactionPersistencePort,
                        installmentTransactionService,
                        financialParticipantNotifier,
                        financialAuditPort,
                        new InstallmentPlanValidationService()
                );
        InstallmentPlanTransactionRequestDTO request = request();

        when(financialTransactionPersistencePort.save(
                org.mockito.ArgumentMatchers.any(InstallmentPlanTransaction.class)
        ))
                .thenAnswer(invocation -> {
                    InstallmentPlanTransaction plan = invocation.getArgument(0);
                    plan.restorePersistenceState(100L, null, null);
                    return plan;
                });

        installmentPlanTransactionService.create(request);

        Map<String, Object> expectedMetadataMap = Map.of(
                "status", "WAITING",
                "type", "INSTALLMENT_PLAN_BLOCK",
                "amount", new BigDecimal("600.00"),
                "installmentsQuantity", 3,
                "installmentDueDay", 10,
                "transactionDate", "2026-07-16"
        );
        var notificationOrder = inOrder(
                financialTransactionPersistencePort,
                financialParticipantNotifier,
                financialAuditPort
        );
        notificationOrder.verify(financialTransactionPersistencePort)
                .save(org.mockito.ArgumentMatchers.any(InstallmentPlanTransaction.class));
        notificationOrder.verify(financialParticipantNotifier)
                .notifyCreation(org.mockito.ArgumentMatchers.any(InstallmentPlanTransaction.class));
        notificationOrder.verify(financialAuditPort)
                .record("INSTALLMENT_PLAN_TRANSACTION_CREATED", 100L, expectedMetadataMap);
        verify(financialAuditPort).record(
                eq("INSTALLMENT_PLAN_TRANSACTION_CREATED"),
                eq(100L),
                eq(expectedMetadataMap)
        );
    }

    @Test
    void auditsSettledInstallmentAfterSavingTheChangedPlan() {
        TestContextRecord testContextRecord = testContextRecord();
        InstallmentPlanTransaction plan = plan(3);
        InstallmentTransaction installment = plan.findInstallment(1);
        installment.restorePersistenceState(101L, null, null);
        installment.settle();
        plan.refreshStatus();
        when(testContextRecord.installmentTransactionService.settle(100L, 1))
                .thenReturn(new InstallmentTransactionService.SettlementResult(plan, true));
        when(testContextRecord.financialTransactionPersistencePort.save(plan)).thenReturn(plan);

        testContextRecord.installmentPlanTransactionService.settleInstallment(100L, 1);

        var notificationOrder = inOrder(
                testContextRecord.financialTransactionPersistencePort,
                testContextRecord.financialParticipantNotifier,
                testContextRecord.financialAuditPort
        );
        notificationOrder.verify(testContextRecord.financialTransactionPersistencePort).save(plan);
        notificationOrder.verify(testContextRecord.financialParticipantNotifier)
                .notifyInstallmentSettlement(installment);
        notificationOrder.verify(testContextRecord.financialAuditPort).record(
                "INSTALLMENT_TRANSACTION_SETTLED",
                101L,
                Map.of(
                        "planId", 100L,
                        "installmentNumber", 1,
                        "totalInstallments", 3,
                        "amount", new BigDecimal("200.00"),
                        "dueDate", "2026-07-10",
                        "status", "SETTLED"
                )
        );
    }

    @Test
    void auditsInstallmentAndPlanWhenLastInstallmentSettlesThePlan() {
        TestContextRecord testContextRecord = testContextRecord();
        InstallmentPlanTransaction plan = plan(2);
        plan.findInstallment(1).settle();
        InstallmentTransaction installment = plan.findInstallment(2);
        installment.restorePersistenceState(101L, null, null);
        installment.settle();
        plan.refreshStatus();
        when(testContextRecord.installmentTransactionService.settle(100L, 2))
                .thenReturn(new InstallmentTransactionService.SettlementResult(plan, true));
        when(testContextRecord.financialTransactionPersistencePort.save(plan)).thenReturn(plan);

        testContextRecord.installmentPlanTransactionService.settleInstallment(100L, 2);

        verify(testContextRecord.financialParticipantNotifier).notifyInstallmentSettlement(installment);
        verify(testContextRecord.financialParticipantNotifier).notifySourceSettlementOnly(plan);
        verify(testContextRecord.financialAuditPort).record(
                "INSTALLMENT_TRANSACTION_SETTLED",
                101L,
                Map.of(
                        "planId", 100L,
                        "installmentNumber", 2,
                        "totalInstallments", 2,
                        "amount", new BigDecimal("300.00"),
                        "dueDate", "2026-08-10",
                        "status", "SETTLED"
                )
        );
        verify(testContextRecord.financialAuditPort).record(
                "INSTALLMENT_PLAN_TRANSACTION_SETTLED",
                100L,
                Map.of(
                        "status", "SETTLED",
                        "amount", new BigDecimal("600.00"),
                        "installmentsQuantity", 2
                )
        );
    }

    @Test
    void doesNotAuditAnAlreadySettledInstallmentAgain() {
        TestContextRecord testContextRecord = testContextRecord();
        InstallmentPlanTransaction plan = plan(2);
        when(testContextRecord.installmentTransactionService.settle(100L, 1))
                .thenReturn(new InstallmentTransactionService.SettlementResult(plan, false));

        testContextRecord.installmentPlanTransactionService.settleInstallment(100L, 1);

        verifyNoInteractions(
                testContextRecord.financialParticipantNotifier,
                testContextRecord.financialAuditPort
        );
    }

    private TestContextRecord testContextRecord() {
        FinancialTransactionPersistencePort financialTransactionPersistencePort =
                mock(FinancialTransactionPersistencePort.class);
        InstallmentTransactionService installmentTransactionService = mock(InstallmentTransactionService.class);
        FinancialParticipantNotifier financialParticipantNotifier = mock(FinancialParticipantNotifier.class);
        FinancialAuditPort financialAuditPort = mock(FinancialAuditPort.class);
        InstallmentPlanTransactionService installmentPlanTransactionService =
                new InstallmentPlanTransactionService(
                        financialTransactionPersistencePort,
                        installmentTransactionService,
                        financialParticipantNotifier,
                        financialAuditPort,
                        new InstallmentPlanValidationService()
                );
        return new TestContextRecord(
                installmentPlanTransactionService,
                financialTransactionPersistencePort,
                installmentTransactionService,
                financialParticipantNotifier,
                financialAuditPort
        );
    }

    private InstallmentPlanTransaction plan(int installmentsQuantity) {
        InstallmentPlanTransaction plan = new InstallmentPlanTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                new BigDecimal("600.00"),
                LocalDate.of(2026, 7, 16),
                "Hospedagem parcelada",
                FinancialTransactionMethod.CREDIT_CARD,
                installmentsQuantity,
                10
        );
        plan.restorePersistenceState(100L, null, null);
        return plan;
    }

    private InstallmentPlanTransactionRequestDTO request() {
        InstallmentPlanTransactionRequestDTO request = new InstallmentPlanTransactionRequestDTO();
        request.senderType = FinancialPartyType.GUEST;
        request.senderId = 20L;
        request.receiverType = FinancialPartyType.CASHIER;
        request.receiverId = 1L;
        request.amount = new BigDecimal("600.00");
        request.transactionDate = LocalDate.of(2026, 7, 16);
        request.description = "Hospedagem parcelada";
        request.method = FinancialTransactionMethod.CREDIT_CARD;
        request.installmentsQuantity = 3;
        request.installmentDueDay = 10;
        return request;
    }

    private record TestContextRecord(
            InstallmentPlanTransactionService installmentPlanTransactionService,
            FinancialTransactionPersistencePort financialTransactionPersistencePort,
            InstallmentTransactionService installmentTransactionService,
            FinancialParticipantNotifier financialParticipantNotifier,
            FinancialAuditPort financialAuditPort
    ) {
    }
}
