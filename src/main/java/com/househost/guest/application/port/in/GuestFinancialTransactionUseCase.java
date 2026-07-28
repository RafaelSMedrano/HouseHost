package com.househost.guest.application.port.in;

public interface GuestFinancialTransactionUseCase {

    void relateFinancialTransaction(Long guestId, Long financialTransactionId);

    void removeFinancialTransactionRelation(Long guestId, Long financialTransactionId);

    void updateFinancialStatus(Long guestId);
}
