package com.househost.finance.financialtransaction.application.port.in;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanMaterializationCommandRecord;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanReplacementCommandRecord;

import java.util.Optional;

public interface FinancialTransactionPlanReplacementUseCase {

    FinancialTransactionPlanReplacementOutcomeDTO replace(
            FinancialTransactionPlanReplacementCommandRecord financialTransactionPlanReplacementCommandRecord
    );

    Optional<FinancialTransactionPlanReplacementOutcomeDTO> materializeForBooking(
            FinancialTransactionPlanMaterializationCommandRecord
                    financialTransactionPlanMaterializationCommandRecord
    );

    FinancialTransactionPlanReplacementOutcomeDTO reconcile(
            Long planId,
            String idempotencyKey
    );
}
