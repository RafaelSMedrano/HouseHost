package com.househost.booking.booking.application.dto;

import com.househost.booking.booking.domain.model.BookingOrigin;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanAllocationDTO;

public class BookingFormCreateRequestDTO {

    public BookingFormGuestDTO guest;
    public BookingFormReservationDTO reservation;
    public FinancialTransactionPlanAllocationDTO paymentAllocation;
    public String idempotencyKey;
    public BookingOrigin origin;
    public BookingStatus status;
    public String specialRequests;
    public String internalNotes;

}
