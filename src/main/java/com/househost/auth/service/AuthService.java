package com.househost.auth.service;

import com.househost.auth.dto.LoginRequestDTO;
import com.househost.auth.dto.LoginResponseDTO;
import com.househost.auth.dto.RegistrationRequestDTO;
import com.househost.auth.dto.RegistrationResponseDTO;
import com.househost.auth.dto.UserPhotoRequestDTO;
import com.househost.auth.dto.UserProfileUpdateRequestDTO;
import com.househost.auth.model.User;
import com.househost.auth.model.UserRole;
import com.househost.auth.repository.UserRepository;
import com.househost.security.JwtService;
import com.househost.shared.exception.InvalidLoginException;
import com.househost.shared.exception.RegistrationException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request == null || isBlank(request.email) || isBlank(request.password)) {
            throw new InvalidLoginException();
        }

        User user = userRepository.findByEmail(request.email.trim())
                .orElseThrow(InvalidLoginException::new);

        if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
            throw new InvalidLoginException();
        }

        String token = jwtService.generateToken(user.getEmail());
        return toLoginResponse(user, token);
    }

    public RegistrationResponseDTO registration(RegistrationRequestDTO request) {
        if (request == null || isBlank(request.username) || isBlank(request.email) || isBlank(request.password)) {
            throw new RegistrationException("Preencha todos os campos.");
        }

        String username = request.username.trim();
        String email = request.email.trim();

        if (userRepository.existsByUsername(username)) {
            throw new RegistrationException("Usuario ja existe.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RegistrationException("Email ja esta cadastrado.");
        }

        String passwordHash = passwordEncoder.encode(request.password);
        UserRole role = parseRole(request.role);
        User user = new User(username, email, passwordHash, role, blankToNull(request.photoUrl));

        userRepository.save(user);

        return new RegistrationResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                role.name(),
                user.getPhotoUrl()
        );
    }

    public List<LoginResponseDTO> quickAccessUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .limit(3)
                .map(this::toLoginResponse)
                .toList();
    }

    public LoginResponseDTO updateUserPhoto(Long id, UserPhotoRequestDTO request) {
        if (id == null) {
            throw new RegistrationException("Usuario invalido.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RegistrationException("Usuario nao encontrado."));

        String photoUrl = request == null ? null : blankToNull(request.photoUrl);
        user.setPhotoUrl(photoUrl);
        userRepository.save(user);

        return toLoginResponse(user);
    }

    public LoginResponseDTO updateUserProfile(Long id, UserProfileUpdateRequestDTO request) {
        if (id == null || request == null) {
            throw new RegistrationException("Usuario invalido.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RegistrationException("Usuario nao encontrado."));

        if (isBlank(request.username) || isBlank(request.email)) {
            throw new RegistrationException("Preencha nome e email.");
        }

        String username = request.username.trim();
        String email = request.email.trim();

        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new RegistrationException("Usuario ja existe.");
        }

        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new RegistrationException("Email ja esta cadastrado.");
        }

        UserRole role = parseRole(request.role);
        user.updateProfile(username, email, blankToNull(request.phone), role);

        if (!isBlank(request.newPassword)) {
            if (isBlank(request.currentPassword) || !passwordEncoder.matches(request.currentPassword, user.getPasswordHash())) {
                throw new RegistrationException("Senha atual invalida.");
            }
            if (request.newPassword.length() < 8) {
                throw new RegistrationException("A nova senha deve ter pelo menos 8 caracteres.");
            }
            user.updatePasswordHash(passwordEncoder.encode(request.newPassword));
        }

        userRepository.save(user);

        return toLoginResponse(user);
    }

    private LoginResponseDTO toLoginResponse(User user) {
        return toLoginResponse(user, null);
    }

    private LoginResponseDTO toLoginResponse(User user, String token) {
        UserRole role = user.getRole() == null ? UserRole.RECEPTION : user.getRole();
        return new LoginResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                role.name(),
                user.getPhotoUrl(),
                token,
                token == null ? null : jwtService.getExpirationSeconds()
        );
    }

    private UserRole parseRole(String value) {
        if (isBlank(value)) {
            return UserRole.RECEPTION;
        }

        String normalized = value.trim()
                .toUpperCase()
                .replace("Ç", "C")
                .replace("Ã", "A")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace(" ", "_")
                .replace("-", "_");

        if (normalized.contains("CEO")) {
            return UserRole.CEO;
        }

        if (normalized.contains("CTO")) {
            return UserRole.CTO;
        }

        return switch (normalized) {
            case "CEO" -> UserRole.CEO;
            case "CTO" -> UserRole.CTO;
            case "ADMIN", "ADMINISTRADOR", "ADMINISTRADORA" -> UserRole.ADMIN;
            case "MANAGER", "GERENTE", "GESTOR", "GESTORA" -> UserRole.MANAGER;
            case "HOUSEKEEPING", "GOVERNANCA", "LIMPEZA" -> UserRole.HOUSEKEEPING;
            case "RECEPTION", "RECEPCAO", "RECEPCIONISTA" -> UserRole.RECEPTION;
            default -> throw new RegistrationException("Cargo invalido.");
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
