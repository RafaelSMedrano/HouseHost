package com.househost.booking.checkout.application.dto;

import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckOutRequestDTO {
    public Long bookingId;
    public LocalDateTime actualCheckOutAt;
    public boolean roomInspected;
    public boolean keysReturned;
    public boolean consumablesChecked;
    public boolean pendingAmountPaid;
    public BigDecimal extraCharges;
    public BigDecimal pendingAmount;
    public String performedBy;
    public String notes;
    public CheckOutRatingRequestDTO rating;
    public FinancialTransactionPlanMaterializationDTO paymentMaterialization;
    public CheckOutStatus status;
}
