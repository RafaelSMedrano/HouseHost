package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanReplacementCommandRecord;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanMaterializationCommandRecord;
import com.househost.finance.financialtransaction.application.records.ReservationFinancialTransactionPlanCommandRecord;
import com.househost.finance.financialtransaction.domain.exception.FinancialTransactionPlanConflictException;
import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlanStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class FinancialTransactionPlanValidationService {

    public String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new FinanceException("Chave de idempotencia do plano financeiro e obrigatoria.");
        }
        String normalizedIdempotencyKey = idempotencyKey.trim();
        if (normalizedIdempotencyKey.length() > 120) {
            throw new FinanceException("Chave de idempotencia excede o limite permitido.");
        }
        return normalizedIdempotencyKey;
    }

    public void validateReservationCommand(
            ReservationFinancialTransactionPlanCommandRecord reservationFinancialTransactionPlanCommandRecord
    ) {
        if (reservationFinancialTransactionPlanCommandRecord == null) {
            throw new FinanceException("Definicao financeira da reserva e obrigatoria.");
        }
        if (reservationFinancialTransactionPlanCommandRecord.bookingId() == null
                || reservationFinancialTransactionPlanCommandRecord.guestId() == null) {
            throw new FinanceException("Reserva e hospede sao obrigatorios para o plano financeiro.");
        }
        if (reservationFinancialTransactionPlanCommandRecord.bookingTotalAmount() == null
                || reservationFinancialTransactionPlanCommandRecord.bookingTotalAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException("Total da reserva deve ser positivo.");
        }
        validateDates(reservationFinancialTransactionPlanCommandRecord);
        validateCurrentPayment(
                reservationFinancialTransactionPlanCommandRecord.currentPaymentAllocationRecord()
        );
        validateDownPayment(
                reservationFinancialTransactionPlanCommandRecord.downPaymentAllocationRecord(),
                reservationFinancialTransactionPlanCommandRecord.checkOutDate()
        );
        validateFuturePayment(
                reservationFinancialTransactionPlanCommandRecord.checkInPaymentAllocationRecord(),
                "check-in"
        );
        validateFuturePayment(
                reservationFinancialTransactionPlanCommandRecord.checkOutPaymentAllocationRecord(),
                "checkout"
        );
        validateExactAllocation(reservationFinancialTransactionPlanCommandRecord);
        normalizeIdempotencyKey(
                reservationFinancialTransactionPlanCommandRecord.idempotencyKey()
        );
    }

    public void validateScheduledPurpose(FinancialTransactionType purpose) {
        if (purpose != FinancialTransactionType.PLAN_CHECK_IN_PAYMENT
                && purpose != FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT) {
            throw new FinanceException("Finalidade agendada deve ser check-in ou checkout.");
        }
    }

    public void validatePlanId(Long planId) {
        if (planId == null) {
            throw new FinanceException("Plano financeiro nao encontrado.");
        }
    }

    public void validateReplacementCommand(
            FinancialTransactionPlanReplacementCommandRecord financialTransactionPlanReplacementCommandRecord
    ) {
        if (financialTransactionPlanReplacementCommandRecord == null) {
            throw new FinanceException("Definicao da substituicao financeira e obrigatoria.");
        }
        validatePlanId(financialTransactionPlanReplacementCommandRecord.planId());
        validateScheduledPurpose(financialTransactionPlanReplacementCommandRecord.purpose());
        if (financialTransactionPlanReplacementCommandRecord
                .scheduledFinancialTransactionId() == null) {
            throw new FinanceException("Pagamento provisorio esperado e obrigatorio.");
        }
        validateReplacementDefinition(
                financialTransactionPlanReplacementCommandRecord.structure(),
                financialTransactionPlanReplacementCommandRecord.method(),
                financialTransactionPlanReplacementCommandRecord.installmentsQuantity(),
                financialTransactionPlanReplacementCommandRecord.idempotencyKey()
        );
    }

    public void validateMaterializationCommand(
            FinancialTransactionPlanMaterializationCommandRecord
                    financialTransactionPlanMaterializationCommandRecord
    ) {
        if (financialTransactionPlanMaterializationCommandRecord == null
                || financialTransactionPlanMaterializationCommandRecord.bookingId() == null) {
            throw new FinanceException("Reserva e obrigatoria para materializar o pagamento.");
        }
        validateScheduledPurpose(financialTransactionPlanMaterializationCommandRecord.purpose());
        if (!financialTransactionPlanMaterializationCommandRecord.materializationRequested()) {
            return;
        }
        validateReplacementDefinition(
                financialTransactionPlanMaterializationCommandRecord.structure(),
                financialTransactionPlanMaterializationCommandRecord.method(),
                financialTransactionPlanMaterializationCommandRecord.installmentsQuantity(),
                financialTransactionPlanMaterializationCommandRecord.idempotencyKey()
        );
    }

    public void validateReplacementCandidate(
            FinancialTransactionPlan financialTransactionPlan,
            FinancialTransaction provisionalFinancialTransaction,
            FinancialTransactionPlanReplacementCommandRecord financialTransactionPlanReplacementCommandRecord,
            LocalDate referenceDate
    ) {
        if (financialTransactionPlan == null || provisionalFinancialTransaction == null) {
            throw replacementConflict("Pagamento provisorio nao pertence mais ao plano financeiro.");
        }
        if (financialTransactionPlan.getStatus() == FinancialTransactionPlanStatus.CANCELED
                || financialTransactionPlan.getStatus()
                == FinancialTransactionPlanStatus.SETTLED) {
            throw replacementConflict("Estado do plano nao permite substituicao financeira.");
        }
        if (provisionalFinancialTransaction.getType()
                != financialTransactionPlanReplacementCommandRecord.purpose()) {
            throw replacementConflict("Finalidade financeira esperada esta desatualizada.");
        }
        long purposeComponentCount = financialTransactionPlan
                .getFinancialTransactionList().stream()
                .filter(financialTransaction -> financialTransaction.getType()
                        == financialTransactionPlanReplacementCommandRecord.purpose())
                .count();
        if (purposeComponentCount != 1L) {
            throw replacementConflict(
                    "Plano financeiro nao possui uma finalidade agendada univoca."
            );
        }
        if (provisionalFinancialTransaction instanceof InstallmentPlanTransaction
                || provisionalFinancialTransaction.getMethod() != null) {
            throw replacementConflict("Componente informado ja possui estrutura definitiva.");
        }
        if (provisionalFinancialTransaction.getStatus() != FinancialTransactionStatus.WAITING
                && provisionalFinancialTransaction.getStatus()
                != FinancialTransactionStatus.OVERDUE) {
            throw replacementConflict("Componente financeiro nao esta elegivel para substituicao.");
        }
        if (provisionalFinancialTransaction.getSettlementDate() != null) {
            throw replacementConflict("Componente com realizacao financeira deve ser preservado.");
        }
        if (referenceDate == null || referenceDate.isAfter(
                financialTransactionPlan.getPlanDueDate()
        )) {
            throw replacementConflict("Prazo do plano financeiro expirou para substituicao.");
        }
        validateReplacementDeadline(
                financialTransactionPlan,
                financialTransactionPlanReplacementCommandRecord,
                referenceDate
        );
    }

    private void validateReplacementDefinition(
            FinancialPaymentStructure structure,
            FinancialTransactionMethod method,
            Integer installmentsQuantity,
            String idempotencyKey
    ) {
        if (structure == null || method == null) {
            throw new FinanceException("Estrutura e forma do pagamento definitivo sao obrigatorias.");
        }
        if (structure == FinancialPaymentStructure.SIMPLE) {
            if (installmentsQuantity != null) {
                throw new FinanceException("Pagamento simples nao aceita quantidade de parcelas.");
            }
        } else if (installmentsQuantity == null || installmentsQuantity < 2
                || installmentsQuantity > 12) {
            throw new FinanceException("Quantidade de parcelas deve estar entre 2 e 12.");
        }
        normalizeIdempotencyKey(idempotencyKey);
    }

    private void validateReplacementDeadline(
            FinancialTransactionPlan financialTransactionPlan,
            FinancialTransactionPlanReplacementCommandRecord financialTransactionPlanReplacementCommandRecord,
            LocalDate referenceDate
    ) {
        if (financialTransactionPlanReplacementCommandRecord.structure()
                != FinancialPaymentStructure.INSTALLMENT) {
            return;
        }
        YearMonth finalInstallmentMonth = YearMonth.from(referenceDate).plusMonths(
                financialTransactionPlanReplacementCommandRecord.installmentsQuantity() - 1L
        );
        LocalDate finalInstallmentDueDate = finalInstallmentMonth.atDay(
                Math.min(referenceDate.getDayOfMonth(), finalInstallmentMonth.lengthOfMonth())
        );
        if (finalInstallmentDueDate.isAfter(financialTransactionPlan.getPlanDueDate())) {
            throw replacementConflict("Parcelamento definitivo excede o prazo do plano financeiro.");
        }
    }

    private FinancialTransactionPlanConflictException replacementConflict(String message) {
        return new FinancialTransactionPlanConflictException(message);
    }

    private void validateDates(
            ReservationFinancialTransactionPlanCommandRecord reservationFinancialTransactionPlanCommandRecord
    ) {
        LocalDate checkInDate = reservationFinancialTransactionPlanCommandRecord.checkInDate();
        LocalDate checkOutDate = reservationFinancialTransactionPlanCommandRecord.checkOutDate();
        if (checkInDate == null || checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
            throw new FinanceException("Datas da reserva sao invalidas para o plano financeiro.");
        }
    }

    private void validateDownPayment(
            ReservationFinancialTransactionPlanCommandRecord.DownPaymentAllocationRecord
                    downPaymentAllocationRecord,
            LocalDate planDueDate
    ) {
        if (downPaymentAllocationRecord == null || !downPaymentAllocationRecord.enabled()) {
            validateDisabledAmount(
                    downPaymentAllocationRecord == null ? null : downPaymentAllocationRecord.amount(),
                    "sinal"
            );
            return;
        }
        validatePositiveAmount(downPaymentAllocationRecord.amount(), "sinal");
        if (!downPaymentAllocationRecord.received()) {
            throw new FinanceException("Sinal deve estar marcado como efetuado.");
        }
        if (downPaymentAllocationRecord.method() == null
                || downPaymentAllocationRecord.structure() == null
                || downPaymentAllocationRecord.paymentDate() == null) {
            throw new FinanceException("Estrutura, forma e data do sinal sao obrigatorias.");
        }
        if (downPaymentAllocationRecord.received()
                && downPaymentAllocationRecord.paymentDate().isAfter(LocalDate.now())) {
            throw new FinanceException("Sinal recebido nao pode possuir data futura.");
        }
        if (downPaymentAllocationRecord.paymentDate().isAfter(planDueDate)) {
            throw new FinanceException("Data do sinal excede o prazo financeiro da reserva.");
        }
        if (downPaymentAllocationRecord.structure() == FinancialPaymentStructure.INSTALLMENT) {
            validateInstallmentDefinition(downPaymentAllocationRecord, planDueDate);
            return;
        }
        if (downPaymentAllocationRecord.installmentsQuantity() != null
                || downPaymentAllocationRecord.installmentDueDay() != null) {
            throw new FinanceException("Sinal a vista nao aceita definicao de parcelas.");
        }
    }

    private void validateInstallmentDefinition(
            ReservationFinancialTransactionPlanCommandRecord.DownPaymentAllocationRecord
                    downPaymentAllocationRecord,
            LocalDate planDueDate
    ) {
        Integer installmentsQuantity = downPaymentAllocationRecord.installmentsQuantity();
        Integer installmentDueDay = downPaymentAllocationRecord.installmentDueDay();
        if (installmentsQuantity == null || installmentsQuantity < 2 || installmentsQuantity > 12) {
            throw new FinanceException("Quantidade de parcelas deve estar entre 2 e 12.");
        }
        if (installmentDueDay == null || installmentDueDay < 1 || installmentDueDay > 31) {
            throw new FinanceException("Dia mensal das parcelas deve estar entre 1 e 31.");
        }
        LocalDate lastInstallmentReferenceDate = downPaymentAllocationRecord.paymentDate()
                .plusMonths(installmentsQuantity - 1L);
        LocalDate lastInstallmentDueDate = lastInstallmentReferenceDate.withDayOfMonth(
                Math.min(installmentDueDay, lastInstallmentReferenceDate.lengthOfMonth())
        );
        if (lastInstallmentDueDate.isAfter(planDueDate)) {
            throw new FinanceException("Parcelamento do sinal excede o prazo do plano financeiro.");
        }
    }

    private void validateFuturePayment(
            ReservationFinancialTransactionPlanCommandRecord.FuturePaymentAllocationRecord
                    futurePaymentAllocationRecord,
            String purpose
    ) {
        if (futurePaymentAllocationRecord == null || !futurePaymentAllocationRecord.enabled()) {
            validateDisabledAmount(
                    futurePaymentAllocationRecord == null
                            ? null
                            : futurePaymentAllocationRecord.amount(),
                    purpose
            );
            return;
        }
        validatePositiveAmount(futurePaymentAllocationRecord.amount(), purpose);
        if (!futurePaymentAllocationRecord.received()) {
            throw new FinanceException("Pagamento de " + purpose + " deve estar marcado como efetuado.");
        }
    }

    private void validateDisabledAmount(BigDecimal amount, String purpose) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) != 0) {
            throw new FinanceException("Alocacao desabilitada de " + purpose + " deve estar vazia.");
        }
    }

    private void validateCurrentPayment(
            ReservationFinancialTransactionPlanCommandRecord.CurrentPaymentAllocationRecord
                    currentPaymentAllocationRecord
    ) {
        if (currentPaymentAllocationRecord == null || !currentPaymentAllocationRecord.enabled()) {
            return;
        }
        validatePositiveAmount(currentPaymentAllocationRecord.amount(), "pagamento no momento");
        if (currentPaymentAllocationRecord.method() == null) {
            throw new FinanceException("Forma de pagamento no momento e obrigatoria.");
        }
        if (!currentPaymentAllocationRecord.received()) {
            throw new FinanceException("Pagamento no momento deve estar marcado como efetuado.");
        }
        if (currentPaymentAllocationRecord.method() == FinancialTransactionMethod.CREDIT_CARD
                && (currentPaymentAllocationRecord.installmentsQuantity() == null
                || currentPaymentAllocationRecord.installmentsQuantity() < 2
                || currentPaymentAllocationRecord.installmentsQuantity() > 12)) {
            throw new FinanceException("Quantidade de parcelas do cartao deve estar entre 2 e 12.");
        }
    }

    private void validatePositiveAmount(BigDecimal amount, String purpose) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException("Valor de " + purpose + " deve ser positivo.");
        }
    }

    private void validateExactAllocation(
            ReservationFinancialTransactionPlanCommandRecord reservationFinancialTransactionPlanCommandRecord
    ) {
        BigDecimal allocatedAmount = enabledAmount(
                reservationFinancialTransactionPlanCommandRecord.currentPaymentAllocationRecord()
        ).add(enabledAmount(
                reservationFinancialTransactionPlanCommandRecord.downPaymentAllocationRecord()
        )).add(enabledAmount(
                reservationFinancialTransactionPlanCommandRecord.checkInPaymentAllocationRecord()
        )).add(enabledAmount(
                reservationFinancialTransactionPlanCommandRecord.checkOutPaymentAllocationRecord()
        ));
        if (allocatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException("Ao menos uma finalidade financeira deve ser alocada.");
        }
        if (allocatedAmount.compareTo(
                reservationFinancialTransactionPlanCommandRecord.bookingTotalAmount()
        ) != 0) {
            throw new FinanceException("Alocacao financeira deve corresponder ao total exato da reserva.");
        }
    }

    private BigDecimal enabledAmount(
            ReservationFinancialTransactionPlanCommandRecord.CurrentPaymentAllocationRecord
                    currentPaymentAllocationRecord
    ) {
        return currentPaymentAllocationRecord != null && currentPaymentAllocationRecord.enabled()
                ? currentPaymentAllocationRecord.amount()
                : BigDecimal.ZERO;
    }

    private BigDecimal enabledAmount(
            ReservationFinancialTransactionPlanCommandRecord.DownPaymentAllocationRecord
                    downPaymentAllocationRecord
    ) {
        return downPaymentAllocationRecord != null && downPaymentAllocationRecord.enabled()
                ? downPaymentAllocationRecord.amount()
                : BigDecimal.ZERO;
    }

    private BigDecimal enabledAmount(
            ReservationFinancialTransactionPlanCommandRecord.FuturePaymentAllocationRecord
                    futurePaymentAllocationRecord
    ) {
        return futurePaymentAllocationRecord != null && futurePaymentAllocationRecord.enabled()
                ? futurePaymentAllocationRecord.amount()
                : BigDecimal.ZERO;
    }
}
