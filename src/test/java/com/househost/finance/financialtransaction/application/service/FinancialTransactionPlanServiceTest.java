package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanCreationOutcomeDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanSummaryDTO;
import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandActorPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandIdempotencyPersistencePort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPlanPersistencePort;
import com.househost.finance.financialtransaction.application.records.FinancialCommandIdempotencyRecord;
import com.househost.finance.financialtransaction.application.records.FinancialCommandOperation;
import com.househost.finance.financialtransaction.application.records.FinancialCommandStatus;
import com.househost.finance.financialtransaction.application.records.ReservationFinancialTransactionPlanCommandRecord;
import com.househost.finance.financialtransaction.domain.exception.FinancialTransactionPlanConflictException;
import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinancialTransactionPlanServiceTest {

    private FinancialTransactionPlanPersistencePort financialTransactionPlanPersistencePort;
    private FinancialCommandIdempotencyPersistencePort financialCommandIdempotencyPersistencePort;
    private FinancialParticipantNotifier financialParticipantNotifier;
    private FinancialAuditPort financialAuditPort;
    private FinancialTransactionPlanService financialTransactionPlanService;

    @BeforeEach
    void setUp() {
        financialTransactionPlanPersistencePort = mock(FinancialTransactionPlanPersistencePort.class);
        financialCommandIdempotencyPersistencePort =
                mock(FinancialCommandIdempotencyPersistencePort.class);
        FinancialCommandActorPort financialCommandActorPort = mock(FinancialCommandActorPort.class);
        financialParticipantNotifier = mock(FinancialParticipantNotifier.class);
        financialAuditPort = mock(FinancialAuditPort.class);
        when(financialCommandActorPort.currentActorReference()).thenReturn("operator@example.invalid");
        when(financialTransactionPlanPersistencePort.save(any()))
                .thenAnswer(invocation -> assignPersistenceIdentity(invocation.getArgument(0)));
        financialTransactionPlanService = new FinancialTransactionPlanService(
                financialTransactionPlanPersistencePort,
                financialCommandIdempotencyPersistencePort,
                financialCommandActorPort,
                financialParticipantNotifier,
                financialAuditPort,
                new FinancialTransactionPlanValidationService()
        );
    }

    @Test
    void createsEffectedSignalAndFuturePaymentsWithoutTheirAudit() {
        when(financialCommandIdempotencyPersistencePort.find(any(), any(), any()))
                .thenReturn(Optional.of(inProgressCommand()));

        FinancialTransactionPlanSummaryDTO financialTransactionPlanSummaryDTO =
                financialTransactionPlanService.createForReservation(command(true, false));

        assertEquals(new BigDecimal("1000.00"), financialTransactionPlanSummaryDTO.getTotalAmount());
        assertEquals(3, financialTransactionPlanSummaryDTO.getComponentSummaryDTOList().size());
        assertEquals(FinancialTransactionType.PLAN_DOWN_PAYMENT.name(),
                financialTransactionPlanSummaryDTO.getComponentSummaryDTOList().get(0).getType());
        assertEquals(FinancialTransactionStatus.SETTLED.name(),
                financialTransactionPlanSummaryDTO.getComponentSummaryDTOList().get(0).getStatus());
        verify(financialParticipantNotifier, times(3)).notifyCreation(any());
        verify(financialParticipantNotifier, times(3)).notifySettlement(any());
        verify(financialAuditPort, times(1)).record(
                org.mockito.ArgumentMatchers.eq("FINANCIAL_TRANSACTION_CREATED"),
                anyLong(),
                any()
        );
        verify(financialAuditPort, never()).record(
                org.mockito.ArgumentMatchers.eq("FINANCIAL_TRANSACTION_CREATED"),
                org.mockito.ArgumentMatchers.eq(102L),
                any()
        );
    }

    @Test
    void createsReceivedInstallmentSignalAsSettledWithResidualCents() {
        when(financialCommandIdempotencyPersistencePort.find(any(), any(), any()))
                .thenReturn(Optional.of(inProgressCommand()));

        ReservationFinancialTransactionPlanCommandRecord commandRecord = command(true, true);
        FinancialTransactionPlanSummaryDTO financialTransactionPlanSummaryDTO =
                financialTransactionPlanService.createForReservation(commandRecord);

        assertEquals("SETTLED", financialTransactionPlanSummaryDTO.getStatus());
        assertEquals("INSTALLMENT",
                financialTransactionPlanSummaryDTO.getComponentSummaryDTOList().get(0).getStructure());
        verify(financialParticipantNotifier).notifySettlement(any(InstallmentPlanTransaction.class));
    }

    @Test
    void createsOneSettledStandardTransactionWhenReservationIsPaidImmediately() {
        when(financialCommandIdempotencyPersistencePort.find(any(), any(), any()))
                .thenReturn(Optional.of(inProgressCommand()));

        FinancialTransactionPlanSummaryDTO financialTransactionPlanSummaryDTO =
                financialTransactionPlanService.createForReservation(currentPaymentCommand());

        assertEquals(1, financialTransactionPlanSummaryDTO.getComponentSummaryDTOList().size());
        assertEquals(FinancialTransactionType.STANDARD.name(),
                financialTransactionPlanSummaryDTO.getComponentSummaryDTOList().get(0).getType());
        assertEquals(FinancialTransactionStatus.SETTLED.name(),
                financialTransactionPlanSummaryDTO.getComponentSummaryDTOList().get(0).getStatus());
        verify(financialParticipantNotifier).notifySettlement(any(FinancialTransaction.class));
    }

    @Test
    void returnsCompletedIdempotentOutcomeWithoutCreatingAnotherCommand() {
        FinancialTransactionPlan financialTransactionPlan = assignPersistenceIdentity(
                new FinancialTransactionPlan(
                        com.househost.finance.financialtransaction.domain.model.FinancialPartyType.GUEST,
                        7L,
                        com.househost.finance.financialtransaction.domain.model.FinancialPartyType.CASHIER,
                        1L,
                        com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType.BOOKING,
                        40L,
                        java.util.List.of(simpleTransaction(new BigDecimal("1000.00"))),
                        LocalDate.now().plusMonths(4),
                        "Plano da reserva"
                )
        );
        FinancialCommandIdempotencyRecord completedCommandRecord = inProgressCommand()
                .complete(40L, 50L);
        when(financialCommandIdempotencyPersistencePort.find(any(), any(), any()))
                .thenReturn(Optional.of(completedCommandRecord));
        when(financialTransactionPlanPersistencePort.findById(50L))
                .thenReturn(Optional.of(financialTransactionPlan));

        Optional<FinancialTransactionPlanCreationOutcomeDTO>
                financialTransactionPlanCreationOutcomeDTOOptional =
                financialTransactionPlanService.prepareReservationCreation("reservation-40");

        assertTrue(financialTransactionPlanCreationOutcomeDTOOptional.isPresent());
        assertEquals(40L, financialTransactionPlanCreationOutcomeDTOOptional.get().getBookingId());
        verify(financialCommandIdempotencyPersistencePort, never()).save(any());
    }

    @Test
    void rejectsInProgressIdempotentCommand() {
        when(financialCommandIdempotencyPersistencePort.find(any(), any(), any()))
                .thenReturn(Optional.of(inProgressCommand()));

        assertThrows(
                FinancialTransactionPlanConflictException.class,
                () -> financialTransactionPlanService.prepareReservationCreation("reservation-40")
        );
    }

    private ReservationFinancialTransactionPlanCommandRecord command(
            boolean received,
            boolean installment
    ) {
        BigDecimal signalAmount = installment ? new BigDecimal("1000.00") : new BigDecimal("200.00");
        return new ReservationFinancialTransactionPlanCommandRecord(
                40L,
                7L,
                new BigDecimal("1000.00"),
                LocalDate.now().plusMonths(2),
                LocalDate.now().plusMonths(4),
                "reservation-40",
                null,
                new ReservationFinancialTransactionPlanCommandRecord.DownPaymentAllocationRecord(
                        true,
                        signalAmount,
                        received,
                        FinancialTransactionMethod.PIX,
                        installment
                                ? FinancialPaymentStructure.INSTALLMENT
                                : FinancialPaymentStructure.SIMPLE,
                        installment ? 3 : null,
                        installment ? 10 : null,
                        LocalDate.now()
                ),
                installment
                        ? null
                        : new ReservationFinancialTransactionPlanCommandRecord
                        .FuturePaymentAllocationRecord(true, new BigDecimal("300.00"), true),
                installment
                        ? null
                        : new ReservationFinancialTransactionPlanCommandRecord
                        .FuturePaymentAllocationRecord(true, new BigDecimal("500.00"), true)
        );
    }

    private ReservationFinancialTransactionPlanCommandRecord currentPaymentCommand() {
        return new ReservationFinancialTransactionPlanCommandRecord(
                40L,
                7L,
                new BigDecimal("1000.00"),
                LocalDate.now().plusMonths(2),
                LocalDate.now().plusMonths(4),
                "reservation-40-current-payment",
                new ReservationFinancialTransactionPlanCommandRecord.CurrentPaymentAllocationRecord(
                        true,
                        new BigDecimal("1000.00"),
                        FinancialTransactionMethod.PIX,
                        null,
                        true
                ),
                null,
                null,
                null
        );
    }

    private FinancialCommandIdempotencyRecord inProgressCommand() {
        return new FinancialCommandIdempotencyRecord(
                1L,
                FinancialCommandOperation.RESERVATION_PLAN_CREATION,
                "operator@example.invalid",
                "reservation-40",
                FinancialCommandStatus.IN_PROGRESS,
                null,
                null,
                null,
                LocalDateTime.now(),
                null
        );
    }

    private FinancialTransactionPlan assignPersistenceIdentity(
            FinancialTransactionPlan financialTransactionPlan
    ) {
        AtomicLong transactionId = new AtomicLong(101L);
        if (financialTransactionPlan.getId() == null) {
            financialTransactionPlan.assignIdentity(50L);
        }
        financialTransactionPlan.getFinancialTransactionList().forEach(financialTransaction -> {
            if (financialTransaction.getId() == null) {
                financialTransaction.restorePersistenceState(
                        transactionId.getAndIncrement(),
                        LocalDate.now(),
                        financialTransaction.getSettlementDate(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );
            }
            if (financialTransaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
                installmentPlanTransaction.getInstallments().forEach(installmentTransaction -> {
                    installmentTransaction.restorePersistenceState(
                            transactionId.getAndIncrement(),
                            LocalDate.now(),
                            installmentTransaction.getSettlementDate(),
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    );
                });
            }
        });
        return financialTransactionPlan;
    }

    private FinancialTransaction simpleTransaction(BigDecimal amount) {
        return new FinancialTransaction(
                com.househost.finance.financialtransaction.domain.model.FinancialPartyType.GUEST,
                7L,
                com.househost.finance.financialtransaction.domain.model.FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionType.PLAN_DOWN_PAYMENT,
                amount,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "Sinal",
                FinancialTransactionMethod.PIX,
                FinancialTransactionStatus.WAITING
        );
    }
}
