package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPersistencePort;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinancialTransactionServiceAuditTest {

    @Test
    void auditsCreationAfterSavingAndNotifyingParticipants() {
        TestContext context = context();
        when(context.persistence.save(any(FinancialTransaction.class))).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0);
            transaction.restorePersistenceState(10L, null, null);
            return transaction;
        });

        context.service.create(request());

        var order = inOrder(context.persistence, context.participantNotifier, context.auditPort);
        order.verify(context.persistence).save(any(FinancialTransaction.class));
        order.verify(context.participantNotifier).notifyCreation(any(FinancialTransaction.class));
        order.verify(context.auditPort).record("FINANCIAL_TRANSACTION_CREATED", 10L, waitingMetadata());
    }

    @Test
    void auditsListAndDetailViews() {
        TestContext context = context();
        FinancialTransaction transaction = transaction();
        when(context.persistence.findAll()).thenReturn(List.of(transaction));
        when(context.persistence.findById(10L)).thenReturn(Optional.of(transaction));

        context.service.findAll();
        context.service.findById(10L);

        verify(context.auditPort).record("FINANCIAL_TRANSACTION_LIST_VIEWED", null, Map.of("resultCount", 1));
        verify(context.auditPort).record("FINANCIAL_TRANSACTION_VIEWED", 10L, Map.of());
    }

    @Test
    void auditsUpdateAfterSaving() {
        TestContext context = context();
        FinancialTransaction transaction = transaction();
        when(context.persistence.findById(10L)).thenReturn(Optional.of(transaction));
        when(context.persistence.save(transaction)).thenReturn(transaction);

        context.service.update(10L, request());

        var order = inOrder(context.persistence, context.auditPort);
        order.verify(context.persistence).save(transaction);
        order.verify(context.auditPort).record("FINANCIAL_TRANSACTION_UPDATED", 10L, waitingMetadata());
    }

    @Test
    void auditsSettlementAfterPersistenceAndNotifications() {
        TestContext context = context();
        FinancialTransaction transaction = transaction();
        when(context.persistence.findById(10L)).thenReturn(Optional.of(transaction));
        when(context.persistence.save(transaction)).thenReturn(transaction);

        context.service.toSettle(10L);

        var order = inOrder(
                context.participantNotifier,
                context.sourceNotifier,
                context.persistence,
                context.auditPort
        );
        order.verify(context.persistence).save(transaction);
        order.verify(context.participantNotifier).notifySettlement(transaction);
        order.verify(context.sourceNotifier).notifySettlement(transaction);
        order.verify(context.auditPort).record("FINANCIAL_TRANSACTION_SETTLED", 10L, settledMetadata());
    }

    @Test
    void auditsDeletionAfterParticipantNotificationAndDeletion() {
        TestContext context = context();
        FinancialTransaction transaction = transaction();
        when(context.persistence.findById(10L)).thenReturn(Optional.of(transaction));

        context.service.delete(10L);

        var order = inOrder(context.participantNotifier, context.persistence, context.auditPort);
        order.verify(context.participantNotifier).notifyDeletion(transaction);
        order.verify(context.persistence).delete(transaction);
        order.verify(context.auditPort).record("FINANCIAL_TRANSACTION_DELETED", 10L, waitingMetadata());
    }

    private TestContext context() {
        FinancialTransactionPersistencePort persistence = mock(FinancialTransactionPersistencePort.class);
        FinancialParticipantNotifier participantNotifier = mock(FinancialParticipantNotifier.class);
        FinancialSourceNotifier sourceNotifier = mock(FinancialSourceNotifier.class);
        FinancialAuditPort auditPort = mock(FinancialAuditPort.class);
        FinancialTransactionService service = new FinancialTransactionService(
                persistence,
                participantNotifier,
                sourceNotifier,
                auditPort,
                new FinancialTransactionValidationService()
        );
        return new TestContext(service, persistence, participantNotifier, sourceNotifier, auditPort);
    }

    private FinancialTransaction transaction() {
        FinancialTransaction transaction = new FinancialTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionType.EXPENSE,
                new BigDecimal("250.00"),
                LocalDate.of(2026, 7, 16),
                "Hospedagem",
                FinancialTransactionMethod.PIX,
                FinancialTransactionStatus.WAITING
        );
        transaction.restorePersistenceState(10L, null, null);
        return transaction;
    }

    private FinancialTransactionRequestDTO request() {
        FinancialTransactionRequestDTO request = new FinancialTransactionRequestDTO();
        request.senderType = FinancialPartyType.GUEST;
        request.senderId = 20L;
        request.receiverType = FinancialPartyType.CASHIER;
        request.receiverId = 1L;
        request.type = FinancialTransactionType.EXPENSE;
        request.amount = new BigDecimal("250.00");
        request.transactionDate = LocalDate.of(2026, 7, 16);
        request.description = "Hospedagem";
        request.method = FinancialTransactionMethod.PIX;
        return request;
    }

    private Map<String, Object> waitingMetadata() {
        return metadata("WAITING");
    }

    private Map<String, Object> settledMetadata() {
        return metadata("SETTLED");
    }

    private Map<String, Object> metadata(String status) {
        return Map.of(
                "status", status,
                "type", "EXPENSE",
                "amount", new BigDecimal("250.00"),
                "transactionDate", "2026-07-16"
        );
    }

    private record TestContext(
            FinancialTransactionService service,
            FinancialTransactionPersistencePort persistence,
            FinancialParticipantNotifier participantNotifier,
            FinancialSourceNotifier sourceNotifier,
            FinancialAuditPort auditPort
    ) {
    }
}
