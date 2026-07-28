package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.port.out.FinancialParty;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FinancialPartyResolver {
    private final Map<FinancialPartyType, FinancialParty> parties;

    public FinancialPartyResolver(List<FinancialParty> parties) {
        this.parties = new EnumMap<>(FinancialPartyType.class);
        parties.forEach(party -> {
            FinancialParty previous = this.parties.put(party.getType(), party);
            if (previous != null) {
                throw new FinanceException("Mais de uma integracao registrada para o mesmo participante financeiro.");
            }
        });
    }

    public FinancialParty resolve(FinancialPartyType type) {
        FinancialParty party = parties.get(type);
        if (party == null) {
            throw new FinanceException("Participante financeiro nao possui integracao registrada.");
        }
        return party;
    }
}
