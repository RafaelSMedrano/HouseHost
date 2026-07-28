package com.househost.finance.cashier.application.dto;

import com.househost.finance.cashier.domain.model.CashierStatus;

import java.math.BigDecimal;

public class CashierRequestDTO {

    public String name;
    public String description;
    public BigDecimal openingBalance;
    public CashierStatus status;
}
