package com.househost.booking.checkout.application.service;

import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanReplacementUseCase;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanMaterializationCommandRecord;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CheckOutFinancialResolver {

    private final FinancialTransactionPlanReplacementUseCase
            financialTransactionPlanReplacementUseCase;

    public CheckOutFinancialResolver(
            FinancialTransactionPlanReplacementUseCase financialTransactionPlanReplacementUseCase
    ) {
        this.financialTransactionPlanReplacementUseCase =
                financialTransactionPlanReplacementUseCase;
    }

    public Optional<FinancialTransactionPlanReplacementOutcomeDTO> resolvePayment(
            CheckOut checkOut,
            FinancialTransactionPlanMaterializationDTO
                    financialTransactionPlanMaterializationDTO
    ) {
        FinancialTransactionPlanMaterializationCommandRecord
                financialTransactionPlanMaterializationCommandRecord =
                new FinancialTransactionPlanMaterializationCommandRecord(
                        checkOut.getBooking().getId(),
                        FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                        financialTransactionPlanMaterializationDTO != null,
                        financialTransactionPlanMaterializationDTO == null
                                ? null
                                : financialTransactionPlanMaterializationDTO.structure,
                        financialTransactionPlanMaterializationDTO == null
                                ? null
                                : financialTransactionPlanMaterializationDTO.method,
                        financialTransactionPlanMaterializationDTO == null
                                ? null
                                : financialTransactionPlanMaterializationDTO.installmentsQuantity,
                        financialTransactionPlanMaterializationDTO == null
                                ? null
                                : financialTransactionPlanMaterializationDTO.idempotencyKey
                );
        return financialTransactionPlanReplacementUseCase.materializeForBooking(
                financialTransactionPlanMaterializationCommandRecord
        );
    }
}
