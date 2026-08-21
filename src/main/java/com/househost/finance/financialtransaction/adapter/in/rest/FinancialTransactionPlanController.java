package com.househost.finance.financialtransaction.adapter.in.rest;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanCreationOutcomeDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanDeadlineRequestDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementRequestDTO;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanReplacementUseCase;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanUseCase;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanReplacementCommandRecord;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.shared.dto.ResponseDTO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financial-transaction-plans")
public class FinancialTransactionPlanController {

    private final FinancialTransactionPlanUseCase financialTransactionPlanUseCase;
    private final FinancialTransactionPlanReplacementUseCase financialTransactionPlanReplacementUseCase;

    public FinancialTransactionPlanController(
            FinancialTransactionPlanUseCase financialTransactionPlanUseCase,
            FinancialTransactionPlanReplacementUseCase financialTransactionPlanReplacementUseCase
    ) {
        this.financialTransactionPlanUseCase = financialTransactionPlanUseCase;
        this.financialTransactionPlanReplacementUseCase =
                financialTransactionPlanReplacementUseCase;
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseDTO findByBookingId(@PathVariable Long bookingId) {
        return new ResponseDTO(
                "success",
                "Plano financeiro da reserva encontrado com sucesso",
                financialTransactionPlanUseCase.findByBookingId(bookingId)
        );
    }

    @GetMapping("/{planId}/scheduled/{purpose}")
    public ResponseDTO findScheduledComponent(
            @PathVariable Long planId,
            @PathVariable FinancialTransactionType purpose
    ) {
        return new ResponseDTO(
                "success",
                "Pagamento agendado encontrado com sucesso",
                financialTransactionPlanUseCase.findScheduledComponent(planId, purpose)
        );
    }

    @GetMapping("/commands/reservation/{idempotencyKey}")
    public ResponseDTO reconcileReservationCreation(@PathVariable String idempotencyKey) {
        FinancialTransactionPlanCreationOutcomeDTO financialTransactionPlanCreationOutcomeDTO =
                financialTransactionPlanUseCase.reconcileReservationCreation(idempotencyKey);
        return new ResponseDTO(
                "success",
                "Resultado do comando financeiro reconciliado com sucesso",
                financialTransactionPlanCreationOutcomeDTO
        );
    }

    @PostMapping("/{planId}/scheduled/{purpose}/replace")
    public ResponseDTO replaceScheduledComponent(
            @PathVariable Long planId,
            @PathVariable FinancialTransactionType purpose,
            @RequestBody FinancialTransactionPlanReplacementRequestDTO request
    ) {
        FinancialTransactionPlanReplacementCommandRecord
                financialTransactionPlanReplacementCommandRecord = request == null
                ? null
                : new FinancialTransactionPlanReplacementCommandRecord(
                        planId,
                        purpose,
                        request.scheduledFinancialTransactionId,
                        request.structure,
                        request.method,
                        request.installmentsQuantity,
                        request.idempotencyKey
                );
        return new ResponseDTO(
                "success",
                "Pagamento financeiro materializado com sucesso",
                financialTransactionPlanReplacementUseCase.replace(
                        financialTransactionPlanReplacementCommandRecord
                )
        );
    }

    @GetMapping("/{planId}/commands/replacement/{idempotencyKey}")
    public ResponseDTO reconcileReplacement(
            @PathVariable Long planId,
            @PathVariable String idempotencyKey
    ) {
        return new ResponseDTO(
                "success",
                "Resultado da materializacao financeira reconciliado com sucesso",
                financialTransactionPlanReplacementUseCase.reconcile(planId, idempotencyKey)
        );
    }

    @GetMapping("/{planId}")
    public ResponseDTO findProfile(@PathVariable Long planId) {
        return new ResponseDTO(
                "success",
                "Perfil do plano financeiro encontrado com sucesso",
                financialTransactionPlanUseCase.findProfile(planId)
        );
    }

    @PatchMapping("/{planId}/deadline")
    public ResponseDTO extendDeadline(
            @PathVariable Long planId,
            @RequestBody FinancialTransactionPlanDeadlineRequestDTO request
    ) {
        return new ResponseDTO(
                "success",
                "Prazo do plano financeiro alterado com sucesso",
                financialTransactionPlanUseCase.extendDeadline(planId, request.planDueDate)
        );
    }

    @PostMapping("/{planId}/cancel")
    public ResponseDTO cancel(@PathVariable Long planId) {
        return new ResponseDTO(
                "success",
                "Plano financeiro cancelado com sucesso",
                financialTransactionPlanUseCase.cancel(planId)
        );
    }

    @DeleteMapping("/{planId}")
    public ResponseDTO delete(@PathVariable Long planId) {
        financialTransactionPlanUseCase.delete(planId);
        return new ResponseDTO(
                "success",
                "Plano financeiro removido com sucesso",
                null
        );
    }
}
