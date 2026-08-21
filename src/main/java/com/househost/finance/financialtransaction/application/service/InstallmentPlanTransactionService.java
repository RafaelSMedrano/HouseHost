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
    private final InstallmentTransactionService installmentTransactionService;
    private final FinancialParticipantNotifier financialParticipantNotifier;
    private final FinancialAuditPort financialAuditPort;
    private final InstallmentPlanValidationService installmentPlanValidationService;

    public InstallmentPlanTransactionService(
            FinancialTransactionPersistencePort transactionPersistence,
            InstallmentTransactionService installmentTransactionService,
            FinancialParticipantNotifier financialParticipantNotifier,
            FinancialAuditPort financialAuditPort,
            InstallmentPlanValidationService installmentPlanValidationService
    ) {
        this.transactionPersistence = transactionPersistence;
        this.installmentTransactionService = installmentTransactionService;
        this.financialParticipantNotifier = financialParticipantNotifier;
        this.financialAuditPort = financialAuditPort;
        this.installmentPlanValidationService = installmentPlanValidationService;
    }

    @Override
    @Transactional
    public InstallmentPlanTransactionResponseDTO create(InstallmentPlanTransactionRequestDTO request) {
        installmentPlanValidationService.validate(request);

        InstallmentPlanTransaction plan = new InstallmentPlanTransaction(
                request.senderType,
                request.senderId,
                request.receiverType,
                request.receiverId,
                request.amount,
                request.transactionDate == null ? LocalDate.now() : request.transactionDate,
                request.description.trim(),
                request.method,
                request.installmentsQuantity,
                request.installmentDueDay
        );
        plan.setSource(request.sourceType, request.sourceId);

        InstallmentPlanTransaction savedPlan = transactionPersistence.save(plan);
        financialParticipantNotifier.notifyCreation(savedPlan);
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
        InstallmentTransactionService.SettlementResult settlementResultRecord =
                installmentTransactionService.settle(planId, installmentNumber);
        InstallmentPlanTransaction plan = settlementResultRecord.plan();
        if (!settlementResultRecord.changed()) {
            return new InstallmentPlanTransactionResponseDTO(plan);
        }
        boolean planSettled = false;
        if (plan.getStatus() != FinancialTransactionStatus.SETTLED && plan.areAllInstallmentsSettled()) {
            toSettle(plan);
            planSettled = true;
        }
        plan = transactionPersistence.save(plan);
        InstallmentTransaction settledInstallment = plan.findInstallment(installmentNumber);
        financialParticipantNotifier.notifyInstallmentSettlement(settledInstallment);
        if (planSettled) {
            financialParticipantNotifier.notifySourceSettlementOnly(plan);
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
