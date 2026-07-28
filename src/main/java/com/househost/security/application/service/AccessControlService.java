package com.househost.security.application.service;

import com.househost.security.application.port.in.AccessControlUseCase;
import com.househost.security.application.port.out.AuthenticationContextPort;
import com.househost.security.application.port.out.SecurityIdentityPort;
import com.househost.security.domain.exception.SecurityAccessDeniedException;
import com.househost.security.domain.model.SecurityIdentity;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AccessControlService implements AccessControlUseCase {

    private static final Set<String> USER_ADMINISTRATORS = Set.of("CEO", "CTO", "ADMIN");

    private final AuthenticationContextPort authenticationContextPort;
    private final SecurityIdentityPort securityIdentityPort;

    public AccessControlService(
            AuthenticationContextPort authenticationContextPort,
            SecurityIdentityPort securityIdentityPort
    ) {
        this.authenticationContextPort = authenticationContextPort;
        this.securityIdentityPort = securityIdentityPort;
    }

    public boolean canManageUsers() {
        SecurityIdentity currentUser = getCurrentUserOrNull();
        return currentUser != null && USER_ADMINISTRATORS.contains(currentUser.role());
    }

    public void requireSelfOrUserAdministrator(Long userId) {
        SecurityIdentity currentUser = getCurrentUserOrNull();
        if (currentUser == null) {
            throw new SecurityAccessDeniedException("Autenticacao obrigatoria.");
        }

        if (!currentUser.id().equals(userId) && !USER_ADMINISTRATORS.contains(currentUser.role())) {
            throw new SecurityAccessDeniedException("Voce nao tem permissao para alterar este usuario.");
        }
    }

    private SecurityIdentity getCurrentUserOrNull() {
        return authenticationContextPort.currentUsername()
                .flatMap(securityIdentityPort::findByEmail)
                .orElse(null);
    }
}
