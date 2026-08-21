package com.househost.booking.checking.application.service;

import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanReplacementUseCase;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanMaterializationCommandRecord;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CheckInFinancialResolver {

    private final FinancialTransactionPlanReplacementUseCase
            financialTransactionPlanReplacementUseCase;

    public CheckInFinancialResolver(
            FinancialTransactionPlanReplacementUseCase financialTransactionPlanReplacementUseCase
    ) {
        this.financialTransactionPlanReplacementUseCase =
                financialTransactionPlanReplacementUseCase;
    }

    public Optional<FinancialTransactionPlanReplacementOutcomeDTO> resolvePayment(
            CheckIn checkIn,
            FinancialTransactionPlanMaterializationDTO
                    financialTransactionPlanMaterializationDTO
    ) {
        FinancialTransactionPlanMaterializationCommandRecord
                financialTransactionPlanMaterializationCommandRecord =
                new FinancialTransactionPlanMaterializationCommandRecord(
                        checkIn.getBooking().getId(),
                        FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
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
