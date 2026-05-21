package com.househost.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CashierExpenseRequestDTO {

    public Long cashierId;
    public Long sourceTransactionId;
    public String description;
    public BigDecimal amount;
    public LocalDate expenseDate;
    public String category;
    public String status;
}
