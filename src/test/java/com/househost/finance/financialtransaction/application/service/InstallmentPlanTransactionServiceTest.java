package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPersistencePort;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
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
        FinancialTransactionPersistencePort persistence = mock(FinancialTransactionPersistencePort.class);
        InstallmentTransactionService installmentService = mock(InstallmentTransactionService.class);
        FinancialParticipantNotifier participantNotifier = mock(FinancialParticipantNotifier.class);
        FinancialSourceNotifier sourceNotifier = mock(FinancialSourceNotifier.class);
        FinancialAuditPort auditPort = mock(FinancialAuditPort.class);
        InstallmentPlanTransactionService service = new InstallmentPlanTransactionService(
                persistence,
                installmentService,
                participantNotifier,
                sourceNotifier,
                auditPort,
                new InstallmentPlanValidationService()
        );
        InstallmentPlanTransactionRequestDTO request = request();

        when(persistence.save(org.mockito.ArgumentMatchers.any(InstallmentPlanTransaction.class)))
                .thenAnswer(invocation -> {
                    InstallmentPlanTransaction plan = invocation.getArgument(0);
                    plan.restorePersistenceState(100L, null, null);
                    return plan;
                });

        service.create(request);

        Map<String, Object> expectedMetadata = Map.of(
                "status", "WAITING",
                "type", "EXPENSE",
                "amount", new BigDecimal("600.00"),
                "installmentsQuantity", 3,
                "installmentDueDay", 10,
                "transactionDate", "2026-07-16"
        );
        var order = inOrder(persistence, participantNotifier, auditPort);
        order.verify(persistence).save(org.mockito.ArgumentMatchers.any(InstallmentPlanTransaction.class));
        order.verify(participantNotifier).notifyCreation(org.mockito.ArgumentMatchers.any(InstallmentPlanTransaction.class));
        order.verify(auditPort).record("INSTALLMENT_PLAN_TRANSACTION_CREATED", 100L, expectedMetadata);
        verify(auditPort).record(eq("INSTALLMENT_PLAN_TRANSACTION_CREATED"), eq(100L), eq(expectedMetadata));
    }

    @Test
    void auditsSettledInstallmentAfterSavingTheChangedPlan() {
        TestContext context = context();
        InstallmentPlanTransaction plan = plan(3);
        InstallmentTransaction installment = plan.findInstallment(1);
        installment.restorePersistenceState(101L, null, null);
        installment.settle();
        plan.refreshStatus();
        when(context.installmentService.settle(100L, 1))
                .thenReturn(new InstallmentTransactionService.SettlementResult(plan, true));
        when(context.persistence.save(plan)).thenReturn(plan);

        context.service.settleInstallment(100L, 1);

        var order = inOrder(context.persistence, context.auditPort);
        order.verify(context.persistence).save(plan);
        order.verify(context.auditPort).record(
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
        TestContext context = context();
        InstallmentPlanTransaction plan = plan(1);
        InstallmentTransaction installment = plan.findInstallment(1);
        installment.restorePersistenceState(101L, null, null);
        installment.settle();
        plan.refreshStatus();
        when(context.installmentService.settle(100L, 1))
                .thenReturn(new InstallmentTransactionService.SettlementResult(plan, true));
        when(context.persistence.save(plan)).thenReturn(plan);

        context.service.settleInstallment(100L, 1);

        verify(context.auditPort).record(
                "INSTALLMENT_TRANSACTION_SETTLED",
                101L,
                Map.of(
                        "planId", 100L,
                        "installmentNumber", 1,
                        "totalInstallments", 1,
                        "amount", new BigDecimal("600.00"),
                        "dueDate", "2026-07-10",
                        "status", "SETTLED"
                )
        );
        verify(context.auditPort).record(
                "INSTALLMENT_PLAN_TRANSACTION_SETTLED",
                100L,
                Map.of(
                        "status", "SETTLED",
                        "amount", new BigDecimal("600.00"),
                        "installmentsQuantity", 1
                )
        );
    }

    @Test
    void doesNotAuditAnAlreadySettledInstallmentAgain() {
        TestContext context = context();
        InstallmentPlanTransaction plan = plan(1);
        when(context.installmentService.settle(100L, 1))
                .thenReturn(new InstallmentTransactionService.SettlementResult(plan, false));

        context.service.settleInstallment(100L, 1);

        verifyNoInteractions(context.auditPort);
    }

    private TestContext context() {
        FinancialTransactionPersistencePort persistence = mock(FinancialTransactionPersistencePort.class);
        InstallmentTransactionService installmentService = mock(InstallmentTransactionService.class);
        FinancialParticipantNotifier participantNotifier = mock(FinancialParticipantNotifier.class);
        FinancialSourceNotifier sourceNotifier = mock(FinancialSourceNotifier.class);
        FinancialAuditPort auditPort = mock(FinancialAuditPort.class);
        InstallmentPlanTransactionService service = new InstallmentPlanTransactionService(
                persistence,
                installmentService,
                participantNotifier,
                sourceNotifier,
                auditPort,
                new InstallmentPlanValidationService()
        );
        return new TestContext(service, persistence, installmentService, auditPort);
    }

    private InstallmentPlanTransaction plan(int installmentsQuantity) {
        InstallmentPlanTransaction plan = new InstallmentPlanTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionType.EXPENSE,
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
        request.type = FinancialTransactionType.EXPENSE;
        request.amount = new BigDecimal("600.00");
        request.transactionDate = LocalDate.of(2026, 7, 16);
        request.description = "Hospedagem parcelada";
        request.method = FinancialTransactionMethod.CREDIT_CARD;
        request.installmentsQuantity = 3;
        request.installmentDueDay = 10;
        return request;
    }

    private record TestContext(
            InstallmentPlanTransactionService service,
            FinancialTransactionPersistencePort persistence,
            InstallmentTransactionService installmentService,
            FinancialAuditPort auditPort
    ) {
    }
}
