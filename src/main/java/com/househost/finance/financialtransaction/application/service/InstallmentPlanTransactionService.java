package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionResponseDTO;
import com.househost.finance.financialtransaction.application.port.in.InstallmentPlanTransactionUseCase;
import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPersistencePort;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransaction;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class InstallmentPlanTransactionService implements InstallmentPlanTransactionUseCase {
    private final FinancialTransactionPersistencePort transactionPersistence;
    private final InstallmentTransactionService installmentService;
    private final FinancialParticipantNotifier participantNotifier;
    private final FinancialSourceNotifier sourceNotifier;
    private final FinancialAuditPort financialAuditPort;
    private final InstallmentPlanValidationService validationService;

    public InstallmentPlanTransactionService(
            FinancialTransactionPersistencePort transactionPersistence,
            InstallmentTransactionService installmentService,
            FinancialParticipantNotifier participantNotifier,
            FinancialSourceNotifier sourceNotifier,
            FinancialAuditPort financialAuditPort,
            InstallmentPlanValidationService validationService
    ) {
        this.transactionPersistence = transactionPersistence;
        this.installmentService = installmentService;
        this.participantNotifier = participantNotifier;
        this.sourceNotifier = sourceNotifier;
        this.financialAuditPort = financialAuditPort;
        this.validationService = validationService;
    }

    @Override
    @Transactional
    public InstallmentPlanTransactionResponseDTO create(InstallmentPlanTransactionRequestDTO request) {
        validationService.validate(request);

        InstallmentPlanTransaction plan = new InstallmentPlanTransaction(
                request.senderType,
                request.senderId,
                request.receiverType,
                request.receiverId,
                request.type,
                request.amount,
                request.transactionDate == null ? LocalDate.now() : request.transactionDate,
                request.description.trim(),
                request.method,
                request.installmentsQuantity,
                request.installmentDueDay
        );
        plan.setSource(request.sourceType, request.sourceId);

        InstallmentPlanTransaction savedPlan = transactionPersistence.save(plan);
        participantNotifier.notifyCreation(savedPlan);
        financialAuditPort.record(
                "INSTALLMENT_PLAN_TRANSACTION_CREATED",
                savedPlan.getId(),
                Map.of(
                        "status", savedPlan.getStatus().name(),
                        "type", savedPlan.getType().name(),
                        "amount", savedPlan.getAmount(),
                        "installmentsQuantity", savedPlan.getInstallmentsQuantity(),
                        "installmentDueDay", savedPlan.getInstallmentDueDay(),
                        "transactionDate", savedPlan.getTransactionDate().toString()
                )
        );
        return new InstallmentPlanTransactionResponseDTO(savedPlan);
    }

    @Override
    @Transactional
    public InstallmentPlanTransactionResponseDTO settleInstallment(Long planId, Integer installmentNumber) {
        InstallmentTransactionService.SettlementResult result = installmentService.settle(planId, installmentNumber);
        InstallmentPlanTransaction plan = result.plan();
        if (!result.changed()) {
            return new InstallmentPlanTransactionResponseDTO(plan);
        }
        boolean planSettled = false;
        if (plan.getStatus() != FinancialTransactionStatus.SETTLED && plan.areAllInstallmentsSettled()) {
            toSettle(plan);
            planSettled = true;
        }
        plan = transactionPersistence.save(plan);
        InstallmentTransaction settledInstallment = plan.findInstallment(installmentNumber);
        participantNotifier.notifySettlement(settledInstallment);
        if (planSettled) {
            participantNotifier.notifySettlement(plan);
            sourceNotifier.notifySettlement(plan);
        }
        recordInstallmentSettlement(plan, settledInstallment);
        if (planSettled) {
            recordPlanSettlement(plan);
        }
        return new InstallmentPlanTransactionResponseDTO(plan);
    }

    private void recordInstallmentSettlement(
            InstallmentPlanTransaction plan,
            InstallmentTransaction installment
    ) {
        financialAuditPort.record(
                "INSTALLMENT_TRANSACTION_SETTLED",
                installment.getId(),
                Map.of(
                        "planId", plan.getId(),
                        "installmentNumber", installment.getInstallmentNumber(),
                        "totalInstallments", installment.getTotalInstallments(),
                        "amount", installment.getAmount(),
                        "dueDate", installment.getDueDate().toString(),
                        "status", installment.getInstallmentStatus().name()
                )
        );
    }

    private void recordPlanSettlement(InstallmentPlanTransaction plan) {
        financialAuditPort.record(
                "INSTALLMENT_PLAN_TRANSACTION_SETTLED",
                plan.getId(),
                Map.of(
                        "status", plan.getStatus().name(),
                        "amount", plan.getAmount(),
                        "installmentsQuantity", plan.getInstallmentsQuantity()
                )
        );
    }

    private void toSettle(InstallmentPlanTransaction plan) {
        plan.setStatus(FinancialTransactionStatus.SETTLED);
    }

}
