package com.househost.stay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckOutRequestDTO {

    public Long stayId;
    public LocalDateTime actualCheckOutAt;
    public boolean roomInspected;
    public boolean keysReturned;
    public boolean consumablesChecked;
    public boolean pendingAmountPaid;
    public BigDecimal extraCharges;
    public BigDecimal pendingAmount;
    public String performedBy;
    public String notes;
    public String status;
}
