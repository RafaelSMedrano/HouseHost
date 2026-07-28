package com.househost.auth.application.service;

import com.househost.auth.application.dto.UserResponseDTO;
import com.househost.auth.application.dto.UserPhotoRequestDTO;
import com.househost.auth.application.dto.UserProfileUpdateRequestDTO;
import com.househost.auth.application.port.out.AuthAuditPort;
import com.househost.auth.application.port.out.PasswordPort;
import com.househost.auth.application.port.out.UserPersistencePort;
import com.househost.auth.application.port.in.UserUseCase;
import com.househost.auth.domain.model.User;
import com.househost.auth.domain.model.UserRole;
import com.househost.security.application.port.in.AccessControlUseCase;
import com.househost.shared.exception.RegistrationException;
import java.util.Map;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserUseCase {
    private final UserPersistencePort persistencePort;
    private final PasswordPort passwordPort;
    private final AuthAuditPort auditPort;
    private final AccessControlUseCase accessControlUseCase;
    private final UserValidationService validationService;

    public UserService(UserPersistencePort persistencePort, PasswordPort passwordPort, AuthAuditPort auditPort,
                       AccessControlUseCase accessControlUseCase, UserValidationService validationService) {
        this.persistencePort = persistencePort;
        this.passwordPort = passwordPort;
        this.auditPort = auditPort;
        this.accessControlUseCase = accessControlUseCase;
        this.validationService = validationService;
    }

    private User requireByEmail(String email) {
        if (email == null || email.isBlank()) throw new RegistrationException("Usuario nao identificado.");
        return persistencePort.findByEmail(email.trim()).orElseThrow(() -> new RegistrationException("Usuario nao encontrado."));
    }

    public UserResponseDTO findByEmail(String email) {
        return toResponse(requireByEmail(email));
    }

    public List<UserResponseDTO> quickAccessUsers() {
        List<UserResponseDTO> users = persistencePort.findFirstThreeByOrderByIdAsc().stream()
                .map(this::toResponse)
                .toList();
        auditPort.recordForJwtActor("USER_LIST_VIEWED", null, Map.of("resultCount", users.size()));
        return users;
    }

    public UserResponseDTO updateUserPhoto(Long id, UserPhotoRequestDTO request) {
        validationService.validatePhotoUpdate(id);
        accessControlUseCase.requireSelfOrUserAdministrator(id);
        User user = findById(id);
        user.setPhotoUrl(request == null ? null : blankToNull(request.photoUrl));
        user = persistencePort.save(user);
        auditPort.recordForJwtActor("USER_PHOTO_UPDATED", user.getId(), Map.of());
        return toResponse(user);
    }

    public UserResponseDTO updateUserProfile(Long id, UserProfileUpdateRequestDTO request) {
        validationService.validateProfileUpdate(id, request);
        accessControlUseCase.requireSelfOrUserAdministrator(id);
        User user = findById(id);
        UserRole role = accessControlUseCase.canManageUsers() && request.role != null ? request.role : user.getRole();
        user.updateProfile(request.username.trim(), request.email.trim(), blankToNull(request.phone), role);

        if (!validationService.isBlank(request.newPassword)) {
            validationService.validatePasswordChange(request, user);
            user.updatePasswordHash(passwordPort.encode(request.newPassword));
        }

        user = persistencePort.save(user);
        auditPort.recordForJwtActor("USER_UPDATED", user.getId(), Map.of("role", role.name()));
        return toResponse(user);
    }

    private User findById(Long id) {
        return persistencePort.findById(id)
                .orElseThrow(() -> new RegistrationException("Usuario nao encontrado."));
    }

    private UserResponseDTO toResponse(User user) {
        UserRole role = user.getRole() == null ? UserRole.RECEPTION : user.getRole();
        return new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.getPhone(), role, user.getPhotoUrl());
    }

    private String blankToNull(String value) {
        return validationService.isBlank(value) ? null : value.trim();
    }
}
