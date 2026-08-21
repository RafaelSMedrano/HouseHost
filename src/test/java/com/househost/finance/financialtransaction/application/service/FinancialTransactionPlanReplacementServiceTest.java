package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandActorPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandIdempotencyPersistencePort;
import com.househost.finance.financialtransaction.application.port.out.FinancialPostCommitAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPlanPersistencePort;
import com.househost.finance.financialtransaction.application.records.FinancialCommandIdempotencyRecord;
import com.househost.finance.financialtransaction.application.records.FinancialCommandStatus;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanMaterializationCommandRecord;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanReplacementCommandRecord;
import com.househost.finance.financialtransaction.domain.exception.FinancialTransactionPlanConflictException;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransaction;
import com.househost.shared.exception.FinanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinancialTransactionPlanReplacementServiceTest {

    private FinancialTransactionPlanPersistencePort financialTransactionPlanPersistencePort;
    private FinancialCommandIdempotencyPersistencePort financialCommandIdempotencyPersistencePort;
    private FinancialParticipantNotifier financialParticipantNotifier;
    private FinancialPostCommitAuditPort financialPostCommitAuditPort;
    private FinancialTransactionPlanReplacementService financialTransactionPlanReplacementService;
    private AtomicReference<FinancialTransactionPlan> financialTransactionPlanReference;
    private AtomicReference<FinancialCommandIdempotencyRecord>
            financialCommandIdempotencyRecordReference;
    private AtomicLong financialTransactionIdSequence;

    @BeforeEach
    void setUp() {
        financialTransactionPlanPersistencePort = mock(
                FinancialTransactionPlanPersistencePort.class
        );
        financialCommandIdempotencyPersistencePort = mock(
                FinancialCommandIdempotencyPersistencePort.class
        );
        FinancialCommandActorPort financialCommandActorPort = mock(
                FinancialCommandActorPort.class
        );
        financialParticipantNotifier = mock(FinancialParticipantNotifier.class);
        financialPostCommitAuditPort = mock(FinancialPostCommitAuditPort.class);
        financialTransactionPlanReference = new AtomicReference<>(initialPlan());
        financialCommandIdempotencyRecordReference = new AtomicReference<>();
        financialTransactionIdSequence = new AtomicLong(201L);

        when(financialCommandActorPort.currentActorReference())
                .thenReturn("operator@example.invalid");
        when(financialTransactionPlanPersistencePort.findByIdForUpdate(50L))
                .thenAnswer(invocation -> Optional.of(financialTransactionPlanReference.get()));
        when(financialTransactionPlanPersistencePort.findById(50L))
                .thenAnswer(invocation -> Optional.of(financialTransactionPlanReference.get()));
        when(financialTransactionPlanPersistencePort.findBySourceForUpdate(
                FinancialTransactionSourceType.BOOKING,
                40L
        )).thenAnswer(invocation -> Optional.of(financialTransactionPlanReference.get()));
        when(financialTransactionPlanPersistencePort.save(any()))
                .thenAnswer(invocation -> persistPlan(invocation.getArgument(0)));
        when(financialCommandIdempotencyPersistencePort.find(any(), any(), any()))
                .thenAnswer(invocation -> Optional.ofNullable(
                        financialCommandIdempotencyRecordReference.get()
                ));
        when(financialCommandIdempotencyPersistencePort.save(any()))
                .thenAnswer(invocation -> {
                    FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord =
                            invocation.getArgument(0);
                    if (financialCommandIdempotencyRecord.id() == null) {
                        financialCommandIdempotencyRecord =
                                new FinancialCommandIdempotencyRecord(
                                        1L,
                                        financialCommandIdempotencyRecord.operation(),
                                        financialCommandIdempotencyRecord.actorReference(),
                                        financialCommandIdempotencyRecord.idempotencyKey(),
                                        financialCommandIdempotencyRecord.status(),
                                        financialCommandIdempotencyRecord.bookingId(),
                                        financialCommandIdempotencyRecord.planId(),
                                        financialCommandIdempotencyRecord
                                                .financialTransactionId(),
                                        financialCommandIdempotencyRecord.createdAt(),
                                        financialCommandIdempotencyRecord.completedAt()
                                );
                    }
                    financialCommandIdempotencyRecordReference.set(
                            financialCommandIdempotencyRecord
                    );
                    return financialCommandIdempotencyRecord;
                });
        financialTransactionPlanReplacementService =
                new FinancialTransactionPlanReplacementService(
                        financialTransactionPlanPersistencePort,
                        financialCommandIdempotencyPersistencePort,
                        financialCommandActorPort,
                        financialParticipantNotifier,
                        financialPostCommitAuditPort,
                        new FinancialTransactionPlanValidationService()
                );
    }

    @Test
    void replacesProvisionalPaymentWithOneDefinitiveSimpleTransaction() {
        FinancialTransactionPlanReplacementOutcomeDTO
                financialTransactionPlanReplacementOutcomeDTO =
                financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.SIMPLE,
                        null,
                        "replacement-simple"
                ));

        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanReference.get();
        FinancialTransaction definitiveFinancialTransaction =
                savedFinancialTransactionPlan.findFinancialTransactionById(
                                financialTransactionPlanReplacementOutcomeDTO
                                        .getDefinitiveComponent().getId()
                        )
                        .orElseThrow();
        assertNotEquals(101L, definitiveFinancialTransaction.getId());
        assertEquals(FinancialTransactionMethod.PIX, definitiveFinancialTransaction.getMethod());
        assertEquals(new BigDecimal("1000.00"), savedFinancialTransactionPlan.getTotalAmount());
        assertFalse(savedFinancialTransactionPlan.containsFinancialTransaction(101L));
        assertFalse(financialTransactionPlanReplacementOutcomeDTO.isIdempotentReplay());
        verify(financialParticipantNotifier).notifyParticipantDeletion(any());
        verify(financialParticipantNotifier).notifySourceDeletionOnly(any());
        verify(financialParticipantNotifier).notifyCreation(definitiveFinancialTransaction);
        verify(financialParticipantNotifier, never()).notifySettlement(any());

        ArgumentCaptor<Map<String, Object>> metadataMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(financialPostCommitAuditPort).recordAfterCommit(
                eq("FINANCIAL_TRANSACTION_CREATED"),
                eq(definitiveFinancialTransaction.getId()),
                metadataMapCaptor.capture()
        );
        assertFalse(metadataMapCaptor.getValue().containsKey("oldId"));
        assertFalse(metadataMapCaptor.getValue().containsKey("replacement"));
    }

    @Test
    void replacesProvisionalPaymentWithInstallmentBlockAndInternalSources() {
        FinancialTransactionPlanReplacementOutcomeDTO
                financialTransactionPlanReplacementOutcomeDTO =
                financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.INSTALLMENT,
                        3,
                        "replacement-installment"
                ));

        FinancialTransaction definitiveFinancialTransaction = financialTransactionPlanReference
                .get()
                .findFinancialTransactionById(
                        financialTransactionPlanReplacementOutcomeDTO
                                .getDefinitiveComponent().getId()
                )
                .orElseThrow();
        InstallmentPlanTransaction installmentPlanTransaction = assertInstanceOf(
                InstallmentPlanTransaction.class,
                definitiveFinancialTransaction
        );
        assertEquals(3, installmentPlanTransaction.getInstallments().size());
        assertEquals(FinancialTransactionSourceType.PLAN,
                installmentPlanTransaction.getSourceType());
        for (InstallmentTransaction installmentTransaction
                : installmentPlanTransaction.getInstallments()) {
            assertEquals(FinancialTransactionSourceType.INSTALLMENT,
                    installmentTransaction.getSourceType());
            assertEquals(installmentPlanTransaction.getId(), installmentTransaction.getSourceId());
        }
        verify(financialPostCommitAuditPort).recordAfterCommit(
                eq("INSTALLMENT_PLAN_TRANSACTION_CREATED"),
                eq(installmentPlanTransaction.getId()),
                any()
        );
    }

    @Test
    void repeatedCompletedCommandReturnsSameAuthoritativeTransaction() {
        FinancialTransactionPlanReplacementCommandRecord
                financialTransactionPlanReplacementCommandRecord = command(
                FinancialPaymentStructure.SIMPLE,
                null,
                "replacement-replay"
        );

        FinancialTransactionPlanReplacementOutcomeDTO firstOutcomeDTO =
                financialTransactionPlanReplacementService.replace(
                        financialTransactionPlanReplacementCommandRecord
                );
        FinancialTransactionPlanReplacementOutcomeDTO replayOutcomeDTO =
                financialTransactionPlanReplacementService.replace(
                        financialTransactionPlanReplacementCommandRecord
                );

        assertEquals(
                firstOutcomeDTO.getDefinitiveComponent().getId(),
                replayOutcomeDTO.getDefinitiveComponent().getId()
        );
        assertTrue(replayOutcomeDTO.isIdempotentReplay());
        verify(financialParticipantNotifier, times(1)).notifyParticipantDeletion(any());
        verify(financialParticipantNotifier, times(1)).notifyCreation(any());
        verify(financialPostCommitAuditPort, times(1)).recordAfterCommit(any(), any(), any());
    }

    @Test
    void materializesCheckoutPurposeFromLockedBookingPlanWithoutClientIdentifiers() {
        FinancialTransactionPlanMaterializationCommandRecord
                financialTransactionPlanMaterializationCommandRecord =
                new FinancialTransactionPlanMaterializationCommandRecord(
                        40L,
                        FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                        true,
                        FinancialPaymentStructure.INSTALLMENT,
                        FinancialTransactionMethod.CREDIT_CARD,
                        2,
                        "checkout-materialization"
                );

        FinancialTransactionPlanReplacementOutcomeDTO
                financialTransactionPlanReplacementOutcomeDTO =
                financialTransactionPlanReplacementService.materializeForBooking(
                                financialTransactionPlanMaterializationCommandRecord
                        )
                        .orElseThrow();

        assertEquals(
                FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT.name(),
                financialTransactionPlanReplacementOutcomeDTO
                        .getDefinitiveComponent().getType()
        );
        assertEquals(
                "INSTALLMENT",
                financialTransactionPlanReplacementOutcomeDTO
                        .getDefinitiveComponent().getStructure()
        );
        assertFalse(financialTransactionPlanReference.get()
                .containsFinancialTransaction(102L));
        verify(financialTransactionPlanPersistencePort).findBySourceForUpdate(
                FinancialTransactionSourceType.BOOKING,
                40L
        );
    }

    @Test
    void requiresDefinitionOnlyWhenBookingHasEligibleScheduledPurpose() {
        FinancialTransactionPlanMaterializationCommandRecord
                missingDefinitionCommandRecord =
                new FinancialTransactionPlanMaterializationCommandRecord(
                        40L,
                        FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                        false,
                        null,
                        null,
                        null,
                        null
                );

        assertThrows(
                FinanceException.class,
                () -> financialTransactionPlanReplacementService.materializeForBooking(
                        missingDefinitionCommandRecord
                )
        );

        when(financialTransactionPlanPersistencePort.findBySourceForUpdate(
                FinancialTransactionSourceType.BOOKING,
                41L
        )).thenReturn(Optional.empty());
        assertTrue(financialTransactionPlanReplacementService.materializeForBooking(
                new FinancialTransactionPlanMaterializationCommandRecord(
                        41L,
                        FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                        false,
                        null,
                        null,
                        null,
                        null
                )
        ).isEmpty());
        verify(financialParticipantNotifier, never()).notifyParticipantDeletion(any());
    }

    @Test
    void rejectsStaleTransactionBeforeParticipantMutation() {
        FinancialTransactionPlanReplacementCommandRecord staleCommandRecord =
                new FinancialTransactionPlanReplacementCommandRecord(
                        50L,
                        FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                        999L,
                        FinancialPaymentStructure.SIMPLE,
                        FinancialTransactionMethod.PIX,
                        null,
                        "replacement-stale"
                );

        assertThrows(
                FinancialTransactionPlanConflictException.class,
                () -> financialTransactionPlanReplacementService.replace(staleCommandRecord)
        );

        verify(financialParticipantNotifier, never()).notifyParticipantDeletion(any());
        verify(financialTransactionPlanPersistencePort, never()).save(any());
        verify(financialPostCommitAuditPort, never()).recordAfterCommit(any(), any(), any());
    }

    @Test
    void rejectsInstallmentScheduleBeyondContractualDeadline() {
        FinancialTransactionPlan financialTransactionPlan = initialPlan(
                LocalDate.now().plusMonths(1)
        );
        financialTransactionPlanReference.set(financialTransactionPlan);

        assertThrows(
                FinancialTransactionPlanConflictException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.INSTALLMENT,
                        3,
                        "replacement-deadline"
                ))
        );

        verify(financialParticipantNotifier, never()).notifyParticipantDeletion(any());
    }

    @Test
    void participantFailurePreventsPersistenceAndAuditScheduling() {
        doThrow(new IllegalStateException("participant failed"))
                .when(financialParticipantNotifier)
                .notifyParticipantDeletion(any());

        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.SIMPLE,
                        null,
                        "replacement-failure"
                ))
        );

        verify(financialTransactionPlanPersistencePort, never()).save(any());
        verify(financialPostCommitAuditPort, never()).recordAfterCommit(any(), any(), any());
    }

    @Test
    void rejectsSettledCanceledAndPartiallyRealizedComponentsBeforeMutation() {
        FinancialTransaction settledFinancialTransaction = provisionalCheckInTransaction();
        settledFinancialTransaction.settle(LocalDate.now());
        financialTransactionPlanReference.get().refreshDerivedState(LocalDate.now());

        assertThrows(
                FinancialTransactionPlanConflictException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.SIMPLE,
                        null,
                        "replacement-settled"
                ))
        );

        financialTransactionPlanReference.set(initialPlan());
        financialTransactionPlanReference.get().cancel();
        assertThrows(
                FinancialTransactionPlanConflictException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.SIMPLE,
                        null,
                        "replacement-canceled"
                ))
        );

        financialTransactionPlanReference.set(initialPlan());
        FinancialTransaction partiallyRealizedFinancialTransaction =
                provisionalCheckInTransaction();
        partiallyRealizedFinancialTransaction.restorePersistenceState(
                partiallyRealizedFinancialTransaction.getId(),
                partiallyRealizedFinancialTransaction.getCreationDate(),
                LocalDate.now(),
                partiallyRealizedFinancialTransaction.getCreatedAt(),
                partiallyRealizedFinancialTransaction.getUpdatedAt()
        );
        assertThrows(
                FinancialTransactionPlanConflictException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.SIMPLE,
                        null,
                        "replacement-partial"
                ))
        );

        verify(financialParticipantNotifier, never()).notifyParticipantDeletion(any());
        verify(financialPostCommitAuditPort, never()).recordAfterCommit(any(), any(), any());
    }

    @Test
    void persistenceAndSourceFailuresDoNotScheduleDefinitiveAudit() {
        doThrow(new IllegalStateException("persistence failed"))
                .when(financialTransactionPlanPersistencePort)
                .save(any());
        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.SIMPLE,
                        null,
                        "replacement-persistence-failure"
                ))
        );
        verify(financialParticipantNotifier, never()).notifyCreation(any());
        verify(financialPostCommitAuditPort, never()).recordAfterCommit(any(), any(), any());

        financialCommandIdempotencyRecordReference.set(null);
        financialTransactionPlanReference.set(initialPlan());
        doAnswer(invocation -> persistPlan(invocation.getArgument(0)))
                .when(financialTransactionPlanPersistencePort)
                .save(any());
        doThrow(new IllegalStateException("source failed"))
                .when(financialParticipantNotifier)
                .notifySourceDeletionOnly(any());
        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.SIMPLE,
                        null,
                        "replacement-source-failure"
                ))
        );
        verify(financialPostCommitAuditPort, never()).recordAfterCommit(any(), any(), any());
    }

    @Test
    void auditRegistrationFailureRemainsObservable() {
        doThrow(new IllegalStateException("audit scheduling failed"))
                .when(financialPostCommitAuditPort)
                .recordAfterCommit(any(), any(), any());

        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        FinancialPaymentStructure.SIMPLE,
                        null,
                        "replacement-audit-failure"
                ))
        );

        verify(financialPostCommitAuditPort).recordAfterCommit(any(), any(), any());
    }

    private FinancialTransactionPlanReplacementCommandRecord command(
            FinancialPaymentStructure financialPaymentStructure,
            Integer installmentsQuantity,
            String idempotencyKey
    ) {
        return new FinancialTransactionPlanReplacementCommandRecord(
                50L,
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                101L,
                financialPaymentStructure,
                FinancialTransactionMethod.PIX,
                installmentsQuantity,
                idempotencyKey
        );
    }

    private FinancialTransactionPlan initialPlan() {
        return initialPlan(LocalDate.now().plusMonths(6));
    }

    private FinancialTransactionPlan initialPlan(LocalDate planDueDate) {
        FinancialTransaction checkInFinancialTransaction = provisionalTransaction(
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                new BigDecimal("600.00"),
                LocalDate.now().plusDays(1)
        );
        FinancialTransaction checkOutFinancialTransaction = provisionalTransaction(
                FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                new BigDecimal("400.00"),
                LocalDate.now().plusMonths(1)
        );
        FinancialTransactionPlan financialTransactionPlan = new FinancialTransactionPlan(
                FinancialPartyType.GUEST,
                7L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionSourceType.BOOKING,
                40L,
                List.of(checkInFinancialTransaction, checkOutFinancialTransaction),
                planDueDate,
                "Plano financeiro da reserva"
        );
        financialTransactionPlan.assignIdentity(50L);
        checkInFinancialTransaction.restorePersistenceState(
                101L,
                LocalDate.now(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        checkOutFinancialTransaction.restorePersistenceState(
                102L,
                LocalDate.now(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        return financialTransactionPlan;
    }

    private FinancialTransaction provisionalTransaction(
            FinancialTransactionType financialTransactionType,
            BigDecimal amount,
            LocalDate dueDate
    ) {
        return new FinancialTransaction(
                FinancialPartyType.GUEST,
                7L,
                FinancialPartyType.CASHIER,
                1L,
                financialTransactionType,
                amount,
                LocalDate.now(),
                dueDate,
                "Pagamento provisorio",
                null,
                FinancialTransactionStatus.WAITING
        );
    }

    private FinancialTransaction provisionalCheckInTransaction() {
        return financialTransactionPlanReference.get()
                .findFinancialTransactionById(101L)
                .orElseThrow();
    }

    private FinancialTransactionPlan persistPlan(
            FinancialTransactionPlan financialTransactionPlan
    ) {
        financialTransactionPlan.getFinancialTransactionList().forEach(
                financialTransaction -> {
                    if (financialTransaction.getId() == null) {
                        financialTransaction.restorePersistenceState(
                                financialTransactionIdSequence.getAndIncrement(),
                                LocalDate.now(),
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        );
                    }
                    if (financialTransaction
                            instanceof InstallmentPlanTransaction installmentPlanTransaction) {
                        installmentPlanTransaction.getInstallments().forEach(
                                installmentTransaction -> {
                                    if (installmentTransaction.getId() == null) {
                                        installmentTransaction.restorePersistenceState(
                                                financialTransactionIdSequence.getAndIncrement(),
                                                LocalDate.now(),
                                                null,
                                                LocalDateTime.now(),
                                                LocalDateTime.now()
                                        );
                                    }
                                }
                        );
                    }
                }
        );
        financialTransactionPlanReference.set(financialTransactionPlan);
        return financialTransactionPlan;
    }
}
