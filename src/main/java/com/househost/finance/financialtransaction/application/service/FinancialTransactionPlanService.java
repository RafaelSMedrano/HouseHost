package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanComponentSummaryDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanCreationOutcomeDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanProfileDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanSummaryDTO;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanUseCase;
import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandActorPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandIdempotencyPersistencePort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPlanPersistencePort;
import com.househost.finance.financialtransaction.application.records.FinancialCommandIdempotencyRecord;
import com.househost.finance.financialtransaction.application.records.FinancialCommandOperation;
import com.househost.finance.financialtransaction.application.records.FinancialCommandStatus;
import com.househost.finance.financialtransaction.application.records.ReservationFinancialTransactionPlanCommandRecord;
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
import com.househost.shared.exception.FinanceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FinancialTransactionPlanService
        implements FinancialTransactionPlanUseCase {

    private static final Long DEFAULT_CASHIER_ID = 1L;
    private static final FinancialCommandOperation RESERVATION_CREATION =
            FinancialCommandOperation.RESERVATION_PLAN_CREATION;

    private final FinancialTransactionPlanPersistencePort financialTransactionPlanPersistencePort;
    private final FinancialCommandIdempotencyPersistencePort financialCommandIdempotencyPersistencePort;
    private final FinancialCommandActorPort financialCommandActorPort;
    private final FinancialParticipantNotifier financialParticipantNotifier;
    private final FinancialAuditPort financialAuditPort;
    private final FinancialTransactionPlanValidationService financialTransactionPlanValidationService;

    public FinancialTransactionPlanService(
            FinancialTransactionPlanPersistencePort financialTransactionPlanPersistencePort,
            FinancialCommandIdempotencyPersistencePort financialCommandIdempotencyPersistencePort,
            FinancialCommandActorPort financialCommandActorPort,
            FinancialParticipantNotifier financialParticipantNotifier,
            FinancialAuditPort financialAuditPort,
            FinancialTransactionPlanValidationService financialTransactionPlanValidationService
    ) {
        this.financialTransactionPlanPersistencePort = financialTransactionPlanPersistencePort;
        this.financialCommandIdempotencyPersistencePort =
                financialCommandIdempotencyPersistencePort;
        this.financialCommandActorPort = financialCommandActorPort;
        this.financialParticipantNotifier = financialParticipantNotifier;
        this.financialAuditPort = financialAuditPort;
        this.financialTransactionPlanValidationService =
                financialTransactionPlanValidationService;
    }

    @Override
    @Transactional
    public Optional<FinancialTransactionPlanCreationOutcomeDTO> prepareReservationCreation(
            String idempotencyKey
    ) {
        String normalizedIdempotencyKey = financialTransactionPlanValidationService
                .normalizeIdempotencyKey(idempotencyKey);
        String actorReference = currentActorReference();
        Optional<FinancialCommandIdempotencyRecord> financialCommandIdempotencyRecordOptional =
                financialCommandIdempotencyPersistencePort.find(
                        RESERVATION_CREATION,
                        actorReference,
                        normalizedIdempotencyKey
                );
        if (financialCommandIdempotencyRecordOptional.isPresent()) {
            FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord =
                    financialCommandIdempotencyRecordOptional.get();
            if (financialCommandIdempotencyRecord.status() == FinancialCommandStatus.COMPLETED) {
                return Optional.of(toOutcome(financialCommandIdempotencyRecord));
            }
            throw idempotencyConflict();
        }

        try {
            financialCommandIdempotencyPersistencePort.save(
                    new FinancialCommandIdempotencyRecord(
                            null,
                            RESERVATION_CREATION,
                            actorReference,
                            normalizedIdempotencyKey,
                            FinancialCommandStatus.IN_PROGRESS,
                            null,
                            null,
                            null,
                            LocalDateTime.now(),
                            null
                    )
            );
        } catch (DataIntegrityViolationException exception) {
            throw idempotencyConflict();
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public FinancialTransactionPlanSummaryDTO createForReservation(
            ReservationFinancialTransactionPlanCommandRecord reservationFinancialTransactionPlanCommandRecord
    ) {
        financialTransactionPlanValidationService.validateReservationCommand(
                reservationFinancialTransactionPlanCommandRecord
        );
        String normalizedIdempotencyKey = financialTransactionPlanValidationService
                .normalizeIdempotencyKey(
                        reservationFinancialTransactionPlanCommandRecord.idempotencyKey()
                );
        FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord =
                findInProgressCommand(normalizedIdempotencyKey);
        FinancialTransactionPlan financialTransactionPlan = createPlan(
                reservationFinancialTransactionPlanCommandRecord
        );
        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistencePort.save(financialTransactionPlan);

        notifyCreatedComponents(savedFinancialTransactionPlan);
        recordDownPaymentCreation(savedFinancialTransactionPlan);
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_PLAN_CREATED",
                savedFinancialTransactionPlan.getId(),
                Map.of(
                        "sourceType", savedFinancialTransactionPlan.getSourceType().name(),
                        "componentCount", savedFinancialTransactionPlan.getFinancialTransactionCount(),
                        "totalAmount", savedFinancialTransactionPlan.getTotalAmount(),
                        "status", savedFinancialTransactionPlan.getStatus().name()
                )
        );
        financialCommandIdempotencyPersistencePort.save(
                financialCommandIdempotencyRecord.complete(
                        reservationFinancialTransactionPlanCommandRecord.bookingId(),
                        savedFinancialTransactionPlan.getId()
                )
        );
        return new FinancialTransactionPlanSummaryDTO(savedFinancialTransactionPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialTransactionPlanCreationOutcomeDTO reconcileReservationCreation(
            String idempotencyKey
    ) {
        String normalizedIdempotencyKey = financialTransactionPlanValidationService
                .normalizeIdempotencyKey(idempotencyKey);
        FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord =
                financialCommandIdempotencyPersistencePort.find(
                                RESERVATION_CREATION,
                                currentActorReference(),
                                normalizedIdempotencyKey
                        )
                        .orElseThrow(() -> new FinanceException(
                                "Comando financeiro idempotente nao encontrado."
                        ));
        if (financialCommandIdempotencyRecord.status() != FinancialCommandStatus.COMPLETED) {
            throw idempotencyConflict();
        }
        return toOutcome(financialCommandIdempotencyRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialTransactionPlanSummaryDTO findByBookingId(Long bookingId) {
        if (bookingId == null) {
            throw new FinanceException("Reserva de origem do plano nao encontrada.");
        }
        FinancialTransactionPlan financialTransactionPlan =
                financialTransactionPlanPersistencePort.findBySource(
                                FinancialTransactionSourceType.BOOKING,
                                bookingId
                        )
                        .orElseThrow(() -> new FinanceException(
                                "Plano financeiro da reserva nao encontrado."
                        ));
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_PLAN_OPERATIONAL_VIEWED",
                financialTransactionPlan.getId(),
                Map.of("sourceType", FinancialTransactionSourceType.BOOKING.name())
        );
        return new FinancialTransactionPlanSummaryDTO(financialTransactionPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialTransactionPlanComponentSummaryDTO findScheduledComponent(
            Long planId,
            FinancialTransactionType purpose
    ) {
        financialTransactionPlanValidationService.validatePlanId(planId);
        financialTransactionPlanValidationService.validateScheduledPurpose(purpose);
        FinancialTransactionPlan financialTransactionPlan = findRequiredPlan(planId);
        FinancialTransaction financialTransaction = financialTransactionPlan
                .getFinancialTransactionList().stream()
                .filter(candidateFinancialTransaction ->
                        candidateFinancialTransaction.getType() == purpose)
                .filter(candidateFinancialTransaction ->
                        candidateFinancialTransaction.getStatus() == FinancialTransactionStatus.WAITING
                                || candidateFinancialTransaction.getStatus()
                                == FinancialTransactionStatus.OVERDUE)
                .findFirst()
                .orElseThrow(() -> new FinanceException(
                        "Pagamento agendado nao encontrado para a finalidade informada."
                ));
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_PLAN_SCHEDULED_COMPONENT_VIEWED",
                financialTransactionPlan.getId(),
                Map.of("purpose", purpose.name())
        );
        return new FinancialTransactionPlanComponentSummaryDTO(financialTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialTransactionPlanProfileDTO findProfile(Long planId) {
        financialTransactionPlanValidationService.validatePlanId(planId);
        FinancialTransactionPlan financialTransactionPlan = findRequiredPlan(planId);
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_PLAN_PROFILE_VIEWED",
                financialTransactionPlan.getId(),
                Map.of("status", financialTransactionPlan.getStatus().name())
        );
        return new FinancialTransactionPlanProfileDTO(financialTransactionPlan);
    }

    @Override
    @Transactional
    public FinancialTransactionPlanProfileDTO extendDeadline(
            Long planId,
            LocalDate planDueDate
    ) {
        financialTransactionPlanValidationService.validatePlanId(planId);
        FinancialTransactionPlan financialTransactionPlan = findRequiredPlanForUpdate(planId);
        financialTransactionPlan.extendPlanDueDate(planDueDate);
        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistencePort.save(financialTransactionPlan);
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_PLAN_DEADLINE_EXTENDED",
                savedFinancialTransactionPlan.getId(),
                Map.of("planDueDate", savedFinancialTransactionPlan.getPlanDueDate().toString())
        );
        return new FinancialTransactionPlanProfileDTO(savedFinancialTransactionPlan);
    }

    @Override
    @Transactional
    public FinancialTransactionPlanProfileDTO cancel(Long planId) {
        financialTransactionPlanValidationService.validatePlanId(planId);
        FinancialTransactionPlan financialTransactionPlan = findRequiredPlanForUpdate(planId);
        financialTransactionPlan.getFinancialTransactionList()
                .forEach(financialParticipantNotifier::notifyParticipantDeletion);
        financialTransactionPlan.cancel();
        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistencePort.save(financialTransactionPlan);
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_PLAN_CANCELED",
                savedFinancialTransactionPlan.getId(),
                Map.of("status", savedFinancialTransactionPlan.getStatus().name())
        );
        return new FinancialTransactionPlanProfileDTO(savedFinancialTransactionPlan);
    }

    @Override
    @Transactional
    public void delete(Long planId) {
        financialTransactionPlanValidationService.validatePlanId(planId);
        FinancialTransactionPlan financialTransactionPlan = findRequiredPlanForUpdate(planId);
        if (!financialTransactionPlan.isEligibleForPhysicalDeletion()) {
            throw new FinanceException("Plano financeiro possui historico e deve ser retido.");
        }
        financialTransactionPlan.getFinancialTransactionList()
                .forEach(financialParticipantNotifier::notifyParticipantDeletion);
        financialTransactionPlanPersistencePort.delete(financialTransactionPlan);
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_PLAN_DELETED",
                planId,
                Map.of("sourceType", financialTransactionPlan.getSourceType().name())
        );
    }

    private FinancialTransactionPlan createPlan(
            ReservationFinancialTransactionPlanCommandRecord reservationFinancialTransactionPlanCommandRecord
    ) {
        List<FinancialTransaction> financialTransactionList = new ArrayList<>();
        addCurrentPayment(financialTransactionList, reservationFinancialTransactionPlanCommandRecord);
        addDownPayment(financialTransactionList, reservationFinancialTransactionPlanCommandRecord);
        addFuturePayment(
                financialTransactionList,
                reservationFinancialTransactionPlanCommandRecord,
                reservationFinancialTransactionPlanCommandRecord.checkInPaymentAllocationRecord(),
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                reservationFinancialTransactionPlanCommandRecord.checkInDate(),
                "Pagamento previsto no check-in"
        );
        addFuturePayment(
                financialTransactionList,
                reservationFinancialTransactionPlanCommandRecord,
                reservationFinancialTransactionPlanCommandRecord.checkOutPaymentAllocationRecord(),
                FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                reservationFinancialTransactionPlanCommandRecord.checkOutDate(),
                "Pagamento previsto no checkout"
        );
        return new FinancialTransactionPlan(
                FinancialPartyType.GUEST,
                reservationFinancialTransactionPlanCommandRecord.guestId(),
                FinancialPartyType.CASHIER,
                DEFAULT_CASHIER_ID,
                FinancialTransactionSourceType.BOOKING,
                reservationFinancialTransactionPlanCommandRecord.bookingId(),
                financialTransactionList,
                reservationFinancialTransactionPlanCommandRecord.checkOutDate(),
                "Plano financeiro da reserva #"
                        + reservationFinancialTransactionPlanCommandRecord.bookingId()
        );
    }

    private void addCurrentPayment(
            List<FinancialTransaction> financialTransactionList,
            ReservationFinancialTransactionPlanCommandRecord reservationFinancialTransactionPlanCommandRecord
    ) {
        ReservationFinancialTransactionPlanCommandRecord.CurrentPaymentAllocationRecord
                currentPaymentAllocationRecord =
                reservationFinancialTransactionPlanCommandRecord.currentPaymentAllocationRecord();
        if (currentPaymentAllocationRecord == null || !currentPaymentAllocationRecord.enabled()) {
            return;
        }
        FinancialTransaction financialTransaction;
        String description = "Pagamento no momento da reserva #"
                + reservationFinancialTransactionPlanCommandRecord.bookingId();
        if (currentPaymentAllocationRecord.method() == FinancialTransactionMethod.CREDIT_CARD) {
            financialTransaction = new InstallmentPlanTransaction(
                    FinancialPartyType.GUEST,
                    reservationFinancialTransactionPlanCommandRecord.guestId(),
                    FinancialPartyType.CASHIER,
                    DEFAULT_CASHIER_ID,
                    currentPaymentAllocationRecord.amount(),
                    LocalDate.now(),
                    description,
                    currentPaymentAllocationRecord.method(),
                    currentPaymentAllocationRecord.installmentsQuantity(),
                    LocalDate.now().getDayOfMonth(),
                    FinancialTransactionType.STANDARD,
                    FinancialTransactionStatus.SETTLED
            );
        } else {
            financialTransaction = new FinancialTransaction(
                    FinancialPartyType.GUEST,
                    reservationFinancialTransactionPlanCommandRecord.guestId(),
                    FinancialPartyType.CASHIER,
                    DEFAULT_CASHIER_ID,
                    FinancialTransactionType.STANDARD,
                    currentPaymentAllocationRecord.amount(),
                    LocalDate.now(),
                    LocalDate.now(),
                    description,
                    currentPaymentAllocationRecord.method(),
                    FinancialTransactionStatus.SETTLED
            );
        }
        financialTransactionList.add(financialTransaction);
    }

    private void addDownPayment(
            List<FinancialTransaction> financialTransactionList,
            ReservationFinancialTransactionPlanCommandRecord reservationFinancialTransactionPlanCommandRecord
    ) {
        ReservationFinancialTransactionPlanCommandRecord.DownPaymentAllocationRecord
                downPaymentAllocationRecord =
                reservationFinancialTransactionPlanCommandRecord.downPaymentAllocationRecord();
        if (downPaymentAllocationRecord == null || !downPaymentAllocationRecord.enabled()) {
            return;
        }
        FinancialTransaction financialTransaction;
        if (downPaymentAllocationRecord.structure() == FinancialPaymentStructure.INSTALLMENT) {
            financialTransaction = new InstallmentPlanTransaction(
                    FinancialPartyType.GUEST,
                    reservationFinancialTransactionPlanCommandRecord.guestId(),
                    FinancialPartyType.CASHIER,
                    DEFAULT_CASHIER_ID,
                    downPaymentAllocationRecord.amount(),
                    downPaymentAllocationRecord.paymentDate(),
                    "Sinal da reserva #"
                            + reservationFinancialTransactionPlanCommandRecord.bookingId(),
                    downPaymentAllocationRecord.method(),
                    downPaymentAllocationRecord.installmentsQuantity(),
                    downPaymentAllocationRecord.installmentDueDay(),
                    FinancialTransactionType.PLAN_DOWN_PAYMENT
            );
        } else {
            financialTransaction = new FinancialTransaction(
                    FinancialPartyType.GUEST,
                    reservationFinancialTransactionPlanCommandRecord.guestId(),
                    FinancialPartyType.CASHIER,
                    DEFAULT_CASHIER_ID,
                    FinancialTransactionType.PLAN_DOWN_PAYMENT,
                    downPaymentAllocationRecord.amount(),
                    LocalDate.now(),
                    downPaymentAllocationRecord.paymentDate(),
                    "Sinal da reserva #"
                            + reservationFinancialTransactionPlanCommandRecord.bookingId(),
                    downPaymentAllocationRecord.method(),
                    FinancialTransactionStatus.WAITING
            );
        }
        if (downPaymentAllocationRecord.received()) {
            financialTransaction.settle(downPaymentAllocationRecord.paymentDate());
        }
        financialTransactionList.add(financialTransaction);
    }

    private void addFuturePayment(
            List<FinancialTransaction> financialTransactionList,
            ReservationFinancialTransactionPlanCommandRecord reservationFinancialTransactionPlanCommandRecord,
            ReservationFinancialTransactionPlanCommandRecord.FuturePaymentAllocationRecord
                    futurePaymentAllocationRecord,
            FinancialTransactionType financialTransactionType,
            LocalDate dueDate,
            String description
    ) {
        if (futurePaymentAllocationRecord == null || !futurePaymentAllocationRecord.enabled()) {
            return;
        }
        financialTransactionList.add(new FinancialTransaction(
                FinancialPartyType.GUEST,
                reservationFinancialTransactionPlanCommandRecord.guestId(),
                FinancialPartyType.CASHIER,
                DEFAULT_CASHIER_ID,
                financialTransactionType,
                futurePaymentAllocationRecord.amount(),
                LocalDate.now(),
                dueDate,
                description + " da reserva #"
                        + reservationFinancialTransactionPlanCommandRecord.bookingId(),
                null,
                futurePaymentAllocationRecord.received()
                        ? FinancialTransactionStatus.SETTLED
                        : FinancialTransactionStatus.WAITING
        ));
    }

    private void notifyCreatedComponents(FinancialTransactionPlan financialTransactionPlan) {
        financialTransactionPlan.getFinancialTransactionList().forEach(financialTransaction -> {
            financialParticipantNotifier.notifyCreation(financialTransaction);
            if (financialTransaction.getStatus() == FinancialTransactionStatus.SETTLED) {
                financialParticipantNotifier.notifySettlement(financialTransaction);
            }
        });
    }

    private void recordDownPaymentCreation(FinancialTransactionPlan financialTransactionPlan) {
        financialTransactionPlan.getFinancialTransactionList().stream()
                .filter(financialTransaction ->
                        financialTransaction.getType() == FinancialTransactionType.PLAN_DOWN_PAYMENT)
                .forEach(financialTransaction -> financialAuditPort.record(
                        financialTransaction instanceof InstallmentPlanTransaction
                                ? "INSTALLMENT_PLAN_TRANSACTION_CREATED"
                                : "FINANCIAL_TRANSACTION_CREATED",
                        financialTransaction.getId(),
                        Map.of(
                                "type", financialTransaction.getType().name(),
                                "amount", financialTransaction.getAmount(),
                                "status", financialTransaction.getStatus().name(),
                                "transactionDate", financialTransaction.getTransactionDate().toString()
                        )
                ));
    }

    private FinancialCommandIdempotencyRecord findInProgressCommand(
            String normalizedIdempotencyKey
    ) {
        FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord =
                financialCommandIdempotencyPersistencePort.find(
                                RESERVATION_CREATION,
                                currentActorReference(),
                                normalizedIdempotencyKey
                        )
                        .orElseThrow(() -> new FinanceException(
                                "Comando de criacao deve ser preparado antes da reserva."
                        ));
        if (financialCommandIdempotencyRecord.status() != FinancialCommandStatus.IN_PROGRESS) {
            throw idempotencyConflict();
        }
        return financialCommandIdempotencyRecord;
    }

    private FinancialTransactionPlanCreationOutcomeDTO toOutcome(
            FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord
    ) {
        FinancialTransactionPlan financialTransactionPlan =
                findRequiredPlan(financialCommandIdempotencyRecord.planId());
        return new FinancialTransactionPlanCreationOutcomeDTO(
                financialCommandIdempotencyRecord.bookingId(),
                new FinancialTransactionPlanSummaryDTO(financialTransactionPlan)
        );
    }

    private FinancialTransactionPlan findRequiredPlan(Long planId) {
        return financialTransactionPlanPersistencePort.findById(planId)
                .orElseThrow(() -> new FinanceException("Plano financeiro nao encontrado."));
    }

    private FinancialTransactionPlan findRequiredPlanForUpdate(Long planId) {
        return financialTransactionPlanPersistencePort.findByIdForUpdate(planId)
                .orElseThrow(() -> new FinanceException("Plano financeiro nao encontrado."));
    }

    private String currentActorReference() {
        String actorReference = financialCommandActorPort.currentActorReference();
        if (actorReference.length() > 180) {
            throw new FinanceException("Identidade autenticada excede o limite permitido.");
        }
        return actorReference;
    }

    private FinancialTransactionPlanConflictException idempotencyConflict() {
        return new FinancialTransactionPlanConflictException(
                "Comando financeiro ja esta em processamento ou foi concluido."
        );
    }
}
