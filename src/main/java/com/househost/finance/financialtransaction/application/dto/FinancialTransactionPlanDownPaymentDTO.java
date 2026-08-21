package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancialTransactionPlanDownPaymentDTO {

    public Boolean enabled;
    public BigDecimal amount;
    public Boolean received;
    public FinancialTransactionMethod method;
    public FinancialPaymentStructure structure;
    public Integer installmentsQuantity;
    public Integer installmentDueDay;
    public LocalDate paymentDate;
}
