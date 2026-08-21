package com.househost.finance.financialtransaction.adapter.out.integration;

import com.househost.finance.financialtransaction.application.port.out.FinancialCommandActorPort;
import com.househost.security.application.port.out.AuthenticationContextPort;
import com.househost.security.domain.exception.SecurityAccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class SecurityFinancialCommandActorAdapter implements FinancialCommandActorPort {

    private final AuthenticationContextPort authenticationContextPort;

    public SecurityFinancialCommandActorAdapter(
            AuthenticationContextPort authenticationContextPort
    ) {
        this.authenticationContextPort = authenticationContextPort;
    }

    @Override
    public String currentActorReference() {
        return authenticationContextPort.currentUsername()
                .orElseThrow(() -> new SecurityAccessDeniedException(
                        "Autenticacao obrigatoria para o comando financeiro."
                ));
    }
}
