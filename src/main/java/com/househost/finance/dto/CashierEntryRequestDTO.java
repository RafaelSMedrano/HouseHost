package com.househost.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CashierEntryRequestDTO {

    public Long cashierId;
    public Long sourceTransactionId;
    public String description;
    public BigDecimal amount;
    public LocalDate entryDate;
    public String source;
    public String status;
}
