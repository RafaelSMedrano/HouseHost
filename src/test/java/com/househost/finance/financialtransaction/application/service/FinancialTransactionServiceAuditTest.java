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
        TestContextRecord testContextRecord = testContextRecord();
        when(testContextRecord.financialTransactionPersistencePort.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> {
                    FinancialTransaction transaction = invocation.getArgument(0);
                    transaction.restorePersistenceState(10L, null, null);
                    return transaction;
                });

        testContextRecord.financialTransactionService.create(request());

        var notificationOrder = inOrder(
                testContextRecord.financialTransactionPersistencePort,
                testContextRecord.financialParticipantNotifier,
                testContextRecord.financialAuditPort
        );
        notificationOrder.verify(testContextRecord.financialTransactionPersistencePort)
                .save(any(FinancialTransaction.class));
        notificationOrder.verify(testContextRecord.financialParticipantNotifier)
                .notifyCreation(any(FinancialTransaction.class));
        notificationOrder.verify(testContextRecord.financialAuditPort)
                .record("FINANCIAL_TRANSACTION_CREATED", 10L, waitingMetadata());
    }

    @Test
    void auditsListAndDetailViews() {
        TestContextRecord testContextRecord = testContextRecord();
        FinancialTransaction transaction = transaction();
        when(testContextRecord.financialTransactionPersistencePort.findAll()).thenReturn(List.of(transaction));
        when(testContextRecord.financialTransactionPersistencePort.findById(10L))
                .thenReturn(Optional.of(transaction));

        testContextRecord.financialTransactionService.findAll();
        testContextRecord.financialTransactionService.findById(10L);

        verify(testContextRecord.financialAuditPort)
                .record("FINANCIAL_TRANSACTION_LIST_VIEWED", null, Map.of("resultCount", 1));
        verify(testContextRecord.financialAuditPort)
                .record("FINANCIAL_TRANSACTION_VIEWED", 10L, Map.of());
    }

    @Test
    void auditsUpdateAfterSaving() {
        TestContextRecord testContextRecord = testContextRecord();
        FinancialTransaction transaction = transaction();
        when(testContextRecord.financialTransactionPersistencePort.findById(10L))
                .thenReturn(Optional.of(transaction));
        when(testContextRecord.financialTransactionPersistencePort.save(transaction)).thenReturn(transaction);

        testContextRecord.financialTransactionService.update(10L, request());

        var notificationOrder = inOrder(
                testContextRecord.financialTransactionPersistencePort,
                testContextRecord.financialAuditPort
        );
        notificationOrder.verify(testContextRecord.financialTransactionPersistencePort).save(transaction);
        notificationOrder.verify(testContextRecord.financialAuditPort)
                .record("FINANCIAL_TRANSACTION_UPDATED", 10L, waitingMetadata());
    }

    @Test
    void auditsSettlementAfterPersistenceAndNotifications() {
        TestContextRecord testContextRecord = testContextRecord();
        FinancialTransaction transaction = transaction();
        when(testContextRecord.financialTransactionPersistencePort.findById(10L))
                .thenReturn(Optional.of(transaction));
        when(testContextRecord.financialTransactionPersistencePort.save(transaction)).thenReturn(transaction);

        testContextRecord.financialTransactionService.toSettle(10L);

        var notificationOrder = inOrder(
                testContextRecord.financialParticipantNotifier,
                testContextRecord.financialTransactionPersistencePort,
                testContextRecord.financialAuditPort
        );
        notificationOrder.verify(testContextRecord.financialTransactionPersistencePort).save(transaction);
        notificationOrder.verify(testContextRecord.financialParticipantNotifier).notifySettlement(transaction);
        notificationOrder.verify(testContextRecord.financialAuditPort)
                .record("FINANCIAL_TRANSACTION_SETTLED", 10L, settledMetadata());
    }

    @Test
    void auditsDeletionAfterParticipantNotificationAndDeletion() {
        TestContextRecord testContextRecord = testContextRecord();
        FinancialTransaction transaction = transaction();
        when(testContextRecord.financialTransactionPersistencePort.findById(10L))
                .thenReturn(Optional.of(transaction));

        testContextRecord.financialTransactionService.delete(10L);

        var notificationOrder = inOrder(
                testContextRecord.financialParticipantNotifier,
                testContextRecord.financialTransactionPersistencePort,
                testContextRecord.financialAuditPort
        );
        notificationOrder.verify(testContextRecord.financialParticipantNotifier).notifyDeletion(transaction);
        notificationOrder.verify(testContextRecord.financialTransactionPersistencePort).delete(transaction);
        notificationOrder.verify(testContextRecord.financialAuditPort)
                .record("FINANCIAL_TRANSACTION_DELETED", 10L, waitingMetadata());
    }

    private TestContextRecord testContextRecord() {
        FinancialTransactionPersistencePort financialTransactionPersistencePort =
                mock(FinancialTransactionPersistencePort.class);
        FinancialParticipantNotifier financialParticipantNotifier = mock(FinancialParticipantNotifier.class);
        FinancialAuditPort financialAuditPort = mock(FinancialAuditPort.class);
        FinancialTransactionService financialTransactionService = new FinancialTransactionService(
                financialTransactionPersistencePort,
                financialParticipantNotifier,
                financialAuditPort,
                new FinancialTransactionValidationService()
        );
        return new TestContextRecord(
                financialTransactionService,
                financialTransactionPersistencePort,
                financialParticipantNotifier,
                financialAuditPort
        );
    }

    private FinancialTransaction transaction() {
        FinancialTransaction transaction = new FinancialTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionType.STANDARD,
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
        request.type = FinancialTransactionType.STANDARD;
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
                "type", "STANDARD",
                "amount", new BigDecimal("250.00"),
                "transactionDate", "2026-07-16"
        );
    }

    private record TestContextRecord(
            FinancialTransactionService financialTransactionService,
            FinancialTransactionPersistencePort financialTransactionPersistencePort,
            FinancialParticipantNotifier financialParticipantNotifier,
            FinancialAuditPort financialAuditPort
    ) {
    }
}
