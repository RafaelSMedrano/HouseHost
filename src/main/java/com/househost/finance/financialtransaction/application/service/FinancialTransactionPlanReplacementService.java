package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanReplacementUseCase;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandActorPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandIdempotencyPersistencePort;
import com.househost.finance.financialtransaction.application.port.out.FinancialPostCommitAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPlanPersistencePort;
import com.househost.finance.financialtransaction.application.records.FinancialCommandIdempotencyRecord;
import com.househost.finance.financialtransaction.application.records.FinancialCommandOperation;
import com.househost.finance.financialtransaction.application.records.FinancialCommandStatus;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanMaterializationCommandRecord;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanReplacementCommandRecord;
import com.househost.finance.financialtransaction.domain.exception.FinancialTransactionPlanConflictException;
import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.shared.exception.FinanceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FinancialTransactionPlanReplacementService
        implements FinancialTransactionPlanReplacementUseCase {

    private static final FinancialCommandOperation REPLACEMENT_OPERATION =
            FinancialCommandOperation.PAYMENT_REPLACEMENT;

    private final FinancialTransactionPlanPersistencePort financialTransactionPlanPersistencePort;
    private final FinancialCommandIdempotencyPersistencePort financialCommandIdempotencyPersistencePort;
    private final FinancialCommandActorPort financialCommandActorPort;
    private final FinancialParticipantNotifier financialParticipantNotifier;
    private final FinancialPostCommitAuditPort financialPostCommitAuditPort;
    private final FinancialTransactionPlanValidationService financialTransactionPlanValidationService;

    public FinancialTransactionPlanReplacementService(
            FinancialTransactionPlanPersistencePort financialTransactionPlanPersistencePort,
            FinancialCommandIdempotencyPersistencePort financialCommandIdempotencyPersistencePort,
            FinancialCommandActorPort financialCommandActorPort,
            FinancialParticipantNotifier financialParticipantNotifier,
            FinancialPostCommitAuditPort financialPostCommitAuditPort,
            FinancialTransactionPlanValidationService financialTransactionPlanValidationService
    ) {
        this.financialTransactionPlanPersistencePort = financialTransactionPlanPersistencePort;
        this.financialCommandIdempotencyPersistencePort =
                financialCommandIdempotencyPersistencePort;
        this.financialCommandActorPort = financialCommandActorPort;
        this.financialParticipantNotifier = financialParticipantNotifier;
        this.financialPostCommitAuditPort = financialPostCommitAuditPort;
        this.financialTransactionPlanValidationService =
                financialTransactionPlanValidationService;
    }

    @Override
    @Transactional
    public FinancialTransactionPlanReplacementOutcomeDTO replace(
            FinancialTransactionPlanReplacementCommandRecord financialTransactionPlanReplacementCommandRecord
    ) {
        financialTransactionPlanValidationService.validateReplacementCommand(
                financialTransactionPlanReplacementCommandRecord
        );
        String normalizedIdempotencyKey = financialTransactionPlanValidationService
                .normalizeIdempotencyKey(
                        financialTransactionPlanReplacementCommandRecord.idempotencyKey()
                );
        String actorReference = currentActorReference();
        FinancialTransactionPlan financialTransactionPlan = findRequiredPlanForUpdate(
                financialTransactionPlanReplacementCommandRecord.planId()
        );
        FinancialCommandIdempotencyRecord existingFinancialCommandIdempotencyRecord =
                findExistingCommand(actorReference, normalizedIdempotencyKey);
        if (existingFinancialCommandIdempotencyRecord != null) {
            return replay(
                    financialTransactionPlan,
                    existingFinancialCommandIdempotencyRecord,
                    true
            );
        }

        return replaceNewComponent(
                financialTransactionPlan,
                financialTransactionPlanReplacementCommandRecord,
                actorReference,
                normalizedIdempotencyKey
        );
    }

    @Override
    @Transactional
    public Optional<FinancialTransactionPlanReplacementOutcomeDTO> materializeForBooking(
            FinancialTransactionPlanMaterializationCommandRecord
                    financialTransactionPlanMaterializationCommandRecord
    ) {
        financialTransactionPlanValidationService.validateMaterializationCommand(
                financialTransactionPlanMaterializationCommandRecord
        );
        Optional<FinancialTransactionPlan> financialTransactionPlanOptional =
                financialTransactionPlanPersistencePort.findBySourceForUpdate(
                        FinancialTransactionSourceType.BOOKING,
                        financialTransactionPlanMaterializationCommandRecord.bookingId()
                );
        if (financialTransactionPlanOptional.isEmpty()) {
            if (financialTransactionPlanMaterializationCommandRecord
                    .materializationRequested()) {
                throw new FinanceException(
                        "Plano financeiro da reserva nao possui pagamento agendado."
                );
            }
            return Optional.empty();
        }

        FinancialTransactionPlan financialTransactionPlan =
                financialTransactionPlanOptional.get();
        Optional<FinancialTransaction> purposeFinancialTransactionOptional =
                findPurposeComponentByType(
                        financialTransactionPlan,
                        financialTransactionPlanMaterializationCommandRecord.purpose()
                );
        if (!financialTransactionPlanMaterializationCommandRecord
                .materializationRequested()) {
            if (purposeFinancialTransactionOptional.filter(
                    this::isEligibleProvisionalComponent
            ).isPresent()) {
                throw new FinanceException(
                        "Definicao do pagamento agendado e obrigatoria para concluir a operacao."
                );
            }
            return Optional.empty();
        }

        String normalizedIdempotencyKey = financialTransactionPlanValidationService
                .normalizeIdempotencyKey(
                        financialTransactionPlanMaterializationCommandRecord.idempotencyKey()
                );
        String actorReference = currentActorReference();
        FinancialCommandIdempotencyRecord existingFinancialCommandIdempotencyRecord =
                findExistingCommand(actorReference, normalizedIdempotencyKey);
        if (existingFinancialCommandIdempotencyRecord != null) {
            return Optional.of(replay(
                    financialTransactionPlan,
                    existingFinancialCommandIdempotencyRecord,
                    true
            ));
        }

        FinancialTransaction provisionalFinancialTransaction =
                purposeFinancialTransactionOptional.orElseThrow(() -> new FinanceException(
                        "Pagamento agendado nao encontrado para a operacao."
                ));
        FinancialTransactionPlanReplacementCommandRecord
                financialTransactionPlanReplacementCommandRecord =
                new FinancialTransactionPlanReplacementCommandRecord(
                        financialTransactionPlan.getId(),
                        financialTransactionPlanMaterializationCommandRecord.purpose(),
                        provisionalFinancialTransaction.getId(),
                        financialTransactionPlanMaterializationCommandRecord.structure(),
                        financialTransactionPlanMaterializationCommandRecord.method(),
                        financialTransactionPlanMaterializationCommandRecord
                                .installmentsQuantity(),
                        normalizedIdempotencyKey
                );
        return Optional.of(replaceNewComponent(
                financialTransactionPlan,
                financialTransactionPlanReplacementCommandRecord,
                actorReference,
                normalizedIdempotencyKey
        ));
    }

    private FinancialTransactionPlanReplacementOutcomeDTO replaceNewComponent(
            FinancialTransactionPlan financialTransactionPlan,
            FinancialTransactionPlanReplacementCommandRecord financialTransactionPlanReplacementCommandRecord,
            String actorReference,
            String normalizedIdempotencyKey
    ) {

        FinancialTransaction provisionalFinancialTransaction = financialTransactionPlan
                .findFinancialTransactionById(
                        financialTransactionPlanReplacementCommandRecord
                                .scheduledFinancialTransactionId()
                )
                .orElseThrow(() -> replacementConflict(
                        "Pagamento provisorio nao pertence mais ao plano financeiro."
                ));
        financialTransactionPlanValidationService.validateReplacementCandidate(
                financialTransactionPlan,
                provisionalFinancialTransaction,
                financialTransactionPlanReplacementCommandRecord,
                LocalDate.now()
        );
        claimCommand(
                financialTransactionPlan.getId(),
                actorReference,
                normalizedIdempotencyKey
        );

        FinancialTransaction definitiveFinancialTransaction = createDefinitiveTransaction(
                financialTransactionPlan,
                provisionalFinancialTransaction,
                financialTransactionPlanReplacementCommandRecord
        );
        definitiveFinancialTransaction.assignPlanMembership(
                financialTransactionPlan.getId(),
                provisionalFinancialTransaction.getPlanComponentOrder()
        );
        financialTransactionPlan.replaceFinancialTransaction(
                provisionalFinancialTransaction.getId(),
                definitiveFinancialTransaction
        );

        financialParticipantNotifier.notifyParticipantDeletion(provisionalFinancialTransaction);
        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistencePort.save(financialTransactionPlan);
        FinancialTransaction savedDefinitiveFinancialTransaction = findPurposeComponent(
                savedFinancialTransactionPlan,
                financialTransactionPlanReplacementCommandRecord
        );
        financialParticipantNotifier.notifySourceDeletionOnly(provisionalFinancialTransaction);
        financialParticipantNotifier.notifyCreation(savedDefinitiveFinancialTransaction);

        FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord =
                requireInProgressCommand(actorReference, normalizedIdempotencyKey);
        financialCommandIdempotencyPersistencePort.save(
                financialCommandIdempotencyRecord.completeReplacement(
                        savedFinancialTransactionPlan.getId(),
                        savedDefinitiveFinancialTransaction.getId()
                )
        );
        scheduleDefinitiveCreationAudit(savedDefinitiveFinancialTransaction);
        return new FinancialTransactionPlanReplacementOutcomeDTO(
                savedDefinitiveFinancialTransaction,
                savedFinancialTransactionPlan,
                false
        );
    }

    private Optional<FinancialTransaction> findPurposeComponentByType(
            FinancialTransactionPlan financialTransactionPlan,
            FinancialTransactionType purpose
    ) {
        List<FinancialTransaction> purposeFinancialTransactionList = financialTransactionPlan
                .getFinancialTransactionList().stream()
                .filter(financialTransaction -> financialTransaction.getType() == purpose)
                .toList();
        if (purposeFinancialTransactionList.size() > 1) {
            throw replacementConflict(
                    "Plano financeiro nao possui uma finalidade agendada univoca."
            );
        }
        return purposeFinancialTransactionList.stream().findFirst();
    }

    private boolean isEligibleProvisionalComponent(FinancialTransaction financialTransaction) {
        return !(financialTransaction instanceof InstallmentPlanTransaction)
                && financialTransaction.getMethod() == null
                && financialTransaction.getSettlementDate() == null
                && (financialTransaction.getStatus() == FinancialTransactionStatus.WAITING
                        || financialTransaction.getStatus() == FinancialTransactionStatus.OVERDUE);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialTransactionPlanReplacementOutcomeDTO reconcile(
            Long planId,
            String idempotencyKey
    ) {
        financialTransactionPlanValidationService.validatePlanId(planId);
        String normalizedIdempotencyKey = financialTransactionPlanValidationService
                .normalizeIdempotencyKey(idempotencyKey);
        FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord =
                financialCommandIdempotencyPersistencePort.find(
                                REPLACEMENT_OPERATION,
                                currentActorReference(),
                                normalizedIdempotencyKey
                        )
                        .orElseThrow(() -> new FinanceException(
                                "Comando de substituicao financeira nao encontrado."
                        ));
        FinancialTransactionPlan financialTransactionPlan =
                financialTransactionPlanPersistencePort.findById(planId)
                        .orElseThrow(() -> new FinanceException(
                                "Plano financeiro nao encontrado."
                        ));
        return replay(financialTransactionPlan, financialCommandIdempotencyRecord, true);
    }

    private FinancialTransaction createDefinitiveTransaction(
            FinancialTransactionPlan financialTransactionPlan,
            FinancialTransaction provisionalFinancialTransaction,
            FinancialTransactionPlanReplacementCommandRecord financialTransactionPlanReplacementCommandRecord
    ) {
        LocalDate transactionDate = LocalDate.now();
        String description = "Pagamento definitivo de "
                + purposeLabel(financialTransactionPlanReplacementCommandRecord)
                + " do plano #"
                + financialTransactionPlan.getId();
        if (financialTransactionPlanReplacementCommandRecord.structure()
                == FinancialPaymentStructure.INSTALLMENT) {
            return new InstallmentPlanTransaction(
                    financialTransactionPlan.getSenderType(),
                    financialTransactionPlan.getSenderId(),
                    financialTransactionPlan.getReceiverType(),
                    financialTransactionPlan.getReceiverId(),
                    provisionalFinancialTransaction.getAmount(),
                    transactionDate,
                    description,
                    financialTransactionPlanReplacementCommandRecord.method(),
                    financialTransactionPlanReplacementCommandRecord.installmentsQuantity(),
                    transactionDate.getDayOfMonth(),
                    financialTransactionPlanReplacementCommandRecord.purpose(),
                    FinancialTransactionStatus.WAITING
            );
        }
        return new FinancialTransaction(
                financialTransactionPlan.getSenderType(),
                financialTransactionPlan.getSenderId(),
                financialTransactionPlan.getReceiverType(),
                financialTransactionPlan.getReceiverId(),
                financialTransactionPlanReplacementCommandRecord.purpose(),
                provisionalFinancialTransaction.getAmount(),
                transactionDate,
                transactionDate,
                description,
                financialTransactionPlanReplacementCommandRecord.method(),
                FinancialTransactionStatus.WAITING
        );
    }

    private String purposeLabel(
            FinancialTransactionPlanReplacementCommandRecord financialTransactionPlanReplacementCommandRecord
    ) {
        return switch (financialTransactionPlanReplacementCommandRecord.purpose()) {
            case PLAN_CHECK_IN_PAYMENT -> "check-in";
            case PLAN_CHECK_OUT_PAYMENT -> "checkout";
            default -> throw new FinanceException("Finalidade financeira agendada e invalida.");
        };
    }

    private FinancialTransaction findPurposeComponent(
            FinancialTransactionPlan financialTransactionPlan,
            FinancialTransactionPlanReplacementCommandRecord financialTransactionPlanReplacementCommandRecord
    ) {
        List<FinancialTransaction> purposeFinancialTransactionList = financialTransactionPlan
                .getFinancialTransactionList().stream()
                .filter(financialTransaction -> financialTransaction.getType()
                        == financialTransactionPlanReplacementCommandRecord.purpose())
                .toList();
        if (purposeFinancialTransactionList.size() != 1) {
            throw new IllegalStateException(
                    "Plano financeiro deve possuir um unico componente para a finalidade."
            );
        }
        return purposeFinancialTransactionList.get(0);
    }

    private void claimCommand(
            Long planId,
            String actorReference,
            String normalizedIdempotencyKey
    ) {
        try {
            financialCommandIdempotencyPersistencePort.save(
                    new FinancialCommandIdempotencyRecord(
                            null,
                            REPLACEMENT_OPERATION,
                            actorReference,
                            normalizedIdempotencyKey,
                            FinancialCommandStatus.IN_PROGRESS,
                            null,
                            planId,
                            null,
                            LocalDateTime.now(),
                            null
                    )
            );
        } catch (DataIntegrityViolationException exception) {
            throw replacementConflict(
                    "Comando de substituicao ja esta em processamento ou foi concluido."
            );
        }
    }

    private FinancialCommandIdempotencyRecord findExistingCommand(
            String actorReference,
            String normalizedIdempotencyKey
    ) {
        return financialCommandIdempotencyPersistencePort.find(
                        REPLACEMENT_OPERATION,
                        actorReference,
                        normalizedIdempotencyKey
                )
                .orElse(null);
    }

    private FinancialCommandIdempotencyRecord requireInProgressCommand(
            String actorReference,
            String normalizedIdempotencyKey
    ) {
        FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord =
                financialCommandIdempotencyPersistencePort.find(
                                REPLACEMENT_OPERATION,
                                actorReference,
                                normalizedIdempotencyKey
                        )
                        .orElseThrow(() -> new IllegalStateException(
                                "Comando idempotente da substituicao nao foi persistido."
                        ));
        if (financialCommandIdempotencyRecord.status() != FinancialCommandStatus.IN_PROGRESS) {
            throw replacementConflict(
                    "Comando de substituicao ja esta em processamento ou foi concluido."
            );
        }
        return financialCommandIdempotencyRecord;
    }

    private FinancialTransactionPlanReplacementOutcomeDTO replay(
            FinancialTransactionPlan financialTransactionPlan,
            FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord,
            boolean idempotentReplay
    ) {
        if (financialCommandIdempotencyRecord.status() != FinancialCommandStatus.COMPLETED) {
            throw replacementConflict(
                    "Comando de substituicao ja esta em processamento."
            );
        }
        if (!financialTransactionPlan.getId().equals(
                financialCommandIdempotencyRecord.planId()
        )) {
            throw replacementConflict("Chave idempotente pertence a outro plano financeiro.");
        }
        FinancialTransaction definitiveFinancialTransaction = financialTransactionPlan
                .findFinancialTransactionById(
                        financialCommandIdempotencyRecord.financialTransactionId()
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Resultado idempotente nao corresponde ao estado autoritativo do plano."
                ));
        return new FinancialTransactionPlanReplacementOutcomeDTO(
                definitiveFinancialTransaction,
                financialTransactionPlan,
                idempotentReplay
        );
    }

    private void scheduleDefinitiveCreationAudit(
            FinancialTransaction definitiveFinancialTransaction
    ) {
        financialPostCommitAuditPort.recordAfterCommit(
                definitiveFinancialTransaction instanceof InstallmentPlanTransaction
                        ? "INSTALLMENT_PLAN_TRANSACTION_CREATED"
                        : "FINANCIAL_TRANSACTION_CREATED",
                definitiveFinancialTransaction.getId(),
                Map.of(
                        "type", definitiveFinancialTransaction.getType().name(),
                        "amount", definitiveFinancialTransaction.getAmount(),
                        "status", definitiveFinancialTransaction.getStatus().name(),
                        "transactionDate",
                        definitiveFinancialTransaction.getTransactionDate().toString()
                )
        );
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

    private FinancialTransactionPlanConflictException replacementConflict(String message) {
        return new FinancialTransactionPlanConflictException(message);
    }
}
