package com.househost.auth.application.service;

import com.househost.auth.application.dto.*;
import com.househost.auth.application.port.out.UserPersistencePort;
import com.househost.auth.application.port.out.PasswordPort;
import com.househost.auth.domain.model.User;
import com.househost.shared.exception.InvalidLoginException;
import com.househost.shared.exception.RegistrationException;
import org.springframework.stereotype.Service;

@Service
public class AuthValidationService {
    private final UserPersistencePort persistencePort;
    private final PasswordPort passwordPort;
    public AuthValidationService(UserPersistencePort persistencePort, PasswordPort passwordPort) { this.persistencePort=persistencePort; this.passwordPort=passwordPort; }
    public void validateLogin(LoginRequestDTO request) { if (request == null || isBlank(request.email) || isBlank(request.password)) throw new InvalidLoginException(); }
    public void validatePassword(String password, User user) { if (!passwordPort.matches(password, user.getPasswordHash())) throw new InvalidLoginException(); }
    public void validateRegistration(RegistrationRequestDTO request) {
        if (request == null || isBlank(request.username) || isBlank(request.email) || isBlank(request.password)) throw new RegistrationException("Preencha todos os campos.");
        String username=request.username.trim(), email=request.email.trim();
        if (persistencePort.existsByUsername(username)) throw new RegistrationException("Usuario ja existe.");
        if (persistencePort.existsByEmail(email)) throw new RegistrationException("Email ja esta cadastrado.");
    }
    public boolean isBlank(String value) { return value == null || value.isBlank(); }
}
