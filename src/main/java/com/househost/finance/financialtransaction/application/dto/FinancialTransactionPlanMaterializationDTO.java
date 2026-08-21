package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;

public class FinancialTransactionPlanMaterializationDTO {

    public FinancialPaymentStructure structure;
    public FinancialTransactionMethod method;
    public Integer installmentsQuantity;
    public String idempotencyKey;
}
