package com.househost.auth.application.service;

import com.househost.auth.application.dto.UserProfileUpdateRequestDTO;
import com.househost.auth.application.port.out.PasswordPort;
import com.househost.auth.application.port.out.UserPersistencePort;
import com.househost.auth.domain.model.User;
import com.househost.shared.exception.RegistrationException;
import org.springframework.stereotype.Service;

@Service
public class UserValidationService {
    private final UserPersistencePort persistencePort;
    private final PasswordPort passwordPort;

    public UserValidationService(UserPersistencePort persistencePort, PasswordPort passwordPort) {
        this.persistencePort = persistencePort;
        this.passwordPort = passwordPort;
    }

    public void validatePhotoUpdate(Long id) {
        if (id == null) {
            throw new RegistrationException("Usuario invalido.");
        }
    }

    public void validateProfileUpdate(Long id, UserProfileUpdateRequestDTO request) {
        if (id == null || request == null) {
            throw new RegistrationException("Usuario invalido.");
        }
        if (isBlank(request.username) || isBlank(request.email)) {
            throw new RegistrationException("Preencha nome e email.");
        }

        String username = request.username.trim();
        String email = request.email.trim();
        if (persistencePort.existsByUsernameAndIdNot(username, id)) {
            throw new RegistrationException("Usuario ja existe.");
        }
        if (persistencePort.existsByEmailAndIdNot(email, id)) {
            throw new RegistrationException("Email ja esta cadastrado.");
        }
    }

    public void validatePasswordChange(UserProfileUpdateRequestDTO request, User user) {
        if (isBlank(request.currentPassword) || !passwordPort.matches(request.currentPassword, user.getPasswordHash())) {
            throw new RegistrationException("Senha atual invalida.");
        }
        if (request.newPassword.length() < 8) {
            throw new RegistrationException("A nova senha deve ter pelo menos 8 caracteres.");
        }
    }

    public boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
