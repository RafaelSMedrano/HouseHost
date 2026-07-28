package com.househost.finance.financialtransaction.application.port.out;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;

public interface FinancialParty {

    FinancialPartyType getType();

    default void onCreate(Long partyId, FinancialTransaction transaction) {
    }

    default void onSettle(Long partyId, FinancialTransaction transaction) {
    }

    default void onDelete(FinancialTransaction transaction) {
    }
}
