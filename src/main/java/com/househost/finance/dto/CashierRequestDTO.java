package com.househost.finance.dto;

import java.math.BigDecimal;

public class CashierRequestDTO {

    public String name;
    public String description;
    public BigDecimal openingBalance;
    public BigDecimal cashOnHand;
    public BigDecimal expectedInflow;
    public BigDecimal expectedOutflow;
    public BigDecimal totalInflow;
    public BigDecimal totalOutflow;
    public String status;
}
