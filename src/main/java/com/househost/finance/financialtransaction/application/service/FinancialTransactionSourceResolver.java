package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionSource;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FinancialTransactionSourceResolver {
    private final Map<FinancialTransactionSourceType, FinancialTransactionSource> sources;

    public FinancialTransactionSourceResolver(List<FinancialTransactionSource> sources) {
        this.sources = new EnumMap<>(FinancialTransactionSourceType.class);
        sources.forEach(source -> {
            FinancialTransactionSource previous = this.sources.put(source.getType(), source);
            if (previous != null) {
                throw new FinanceException("Mais de uma integracao registrada para a mesma origem financeira.");
            }
        });
    }

    public FinancialTransactionSource resolve(FinancialTransactionSourceType type) {
        FinancialTransactionSource source = sources.get(type);
        if (source == null) {
            throw new FinanceException("Origem da transacao financeira nao possui integracao registrada.");
        }
        return source;
    }
}
