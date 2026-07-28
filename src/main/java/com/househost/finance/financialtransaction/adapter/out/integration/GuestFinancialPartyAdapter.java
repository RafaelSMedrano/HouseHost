package com.househost.finance.financialtransaction.adapter.out.integration;

import com.househost.finance.financialtransaction.application.port.out.FinancialParty;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.guest.application.port.in.GuestFinancialTransactionUseCase;
import com.househost.guest.application.service.GuestService;
import org.springframework.stereotype.Component;

@Component
public class GuestFinancialPartyAdapter implements FinancialParty {

    private static final FinancialPartyType TYPE = FinancialPartyType.GUEST;

    private final GuestService guestService;
    private final GuestFinancialTransactionUseCase guestFinancialTransactionUseCase;

    public GuestFinancialPartyAdapter(GuestService guestService, GuestFinancialTransactionUseCase guestFinancialTransactionUseCase) {
        this.guestService = guestService;
        this.guestFinancialTransactionUseCase = guestFinancialTransactionUseCase;
    }

    @Override
    public void onCreate(Long partyId, FinancialTransaction transaction) {
        guestService.findGuestById(partyId);
        guestFinancialTransactionUseCase.relateFinancialTransaction(partyId, transaction.getId());
    }

    @Override
    public FinancialPartyType getType() {
        return TYPE;
    }

    @Override
    public void onSettle(Long partyId, FinancialTransaction transaction) {
        guestFinancialTransactionUseCase.updateFinancialStatus(partyId);
    }

    @Override
    public void onDelete(FinancialTransaction transaction) {
        if (transaction.getSenderType() == TYPE) {
            guestFinancialTransactionUseCase.removeFinancialTransactionRelation(transaction.getSenderId(), transaction.getId());
        }
        if (transaction.getReceiverType() == TYPE) {
            guestFinancialTransactionUseCase.removeFinancialTransactionRelation(transaction.getReceiverId(), transaction.getId());
        }
    }
}
