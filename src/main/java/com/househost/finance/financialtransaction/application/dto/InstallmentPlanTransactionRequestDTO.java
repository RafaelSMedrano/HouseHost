package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InstallmentPlanTransactionRequestDTO {
    public FinancialPartyType senderType;
    public Long senderId;
    public FinancialPartyType receiverType;
    public Long receiverId;
    public FinancialTransactionSourceType sourceType;
    public Long sourceId;
    public FinancialTransactionType type;
    public BigDecimal amount;
    public LocalDate transactionDate;
    public String description;
    public FinancialTransactionMethod method;
    public Integer installmentsQuantity;
    public Integer installmentDueDay;
}
