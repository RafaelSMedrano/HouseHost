package com.househost.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancialTransactionRequestDTO {

    public String senderType;
    public Long senderId;
    public String receiverType;
    public Long receiverId;
    public Long guestId;
    public String sourceType;
    public Long sourceId;
    public String type;
    public BigDecimal amount;
    public LocalDate transactionDate;
    public String description;
    public String status;
    public String method;
}
