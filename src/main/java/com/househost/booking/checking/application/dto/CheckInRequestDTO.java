package com.househost.booking.checking.application.dto;

import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;

public class CheckInRequestDTO {

    public Long bookingId;
    public Long guestId;
    public Long roomId;
    public Integer adults;
    public Integer children;
    public Integer pets;
    public boolean documentVerified;
    public boolean paymentVerified;
    public boolean registrationFormSigned;
    public boolean rulesAccepted;
    public boolean keysDelivered;
    public String vehiclePlate;
    public String vehicleModel;
    public String performedBy;
    public String notes;
    public FinancialTransactionPlanMaterializationDTO paymentMaterialization;
    public CheckInStatus status;
}
