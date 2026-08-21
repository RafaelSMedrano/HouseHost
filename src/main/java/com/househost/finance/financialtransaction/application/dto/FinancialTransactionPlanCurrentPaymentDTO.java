package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;

import java.math.BigDecimal;

public class FinancialTransactionPlanCurrentPaymentDTO {

    public Boolean enabled;
    public BigDecimal amount;
    public FinancialTransactionMethod method;
    public Integer installmentsQuantity;
    public Boolean received;
}
