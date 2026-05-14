package com.househost.auth.service;

import com.househost.auth.dto.LoginRequestDTO;
import com.househost.auth.dto.LoginResponseDTO;
import com.househost.auth.dto.RegistrationRequestDTO;
import com.househost.auth.dto.RegistrationResponseDTO;
import com.househost.auth.model.User;
import com.househost.auth.model.UserRole;
import com.househost.auth.repository.UserRepository;
import com.househost.shared.dto.ResponseDTO;
import com.househost.shared.exception.InvalidLoginException;
import com.househost.shared.exception.RegistrationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseDTO login(LoginRequestDTO request) {
        if (request == null || request.username == null || request.password == null) {
            throw new InvalidLoginException();
        }

        User user = userRepository.findByUsername(request.username)
                .orElseThrow(InvalidLoginException::new);

        if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
            throw new InvalidLoginException();
        }

        LoginResponseDTO loginData = new LoginResponseDTO(
                user.getUsername(),
                user.getRole().name()
        );

        return new ResponseDTO("success", "Login realizado com sucesso", loginData);
    }

    public ResponseDTO registration(RegistrationRequestDTO request) {
        if (request == null || isBlank(request.username) || isBlank(request.email) || isBlank(request.password)) {
            throw new RegistrationException("Preencha todos os campos.");
        }

        if (userRepository.existsByUsername(request.username)) {
            throw new RegistrationException("Usuario ja existe.");
        }

        if (userRepository.existsByEmail(request.email)) {
            throw new RegistrationException("Email ja esta cadastrado.");
        }

        String passwordHash = passwordEncoder.encode(request.password);
        User user = new User(request.username, request.email, passwordHash, UserRole.ADMIN);

        userRepository.save(user);

        RegistrationResponseDTO registrationData = new RegistrationResponseDTO(user.getUsername());

        return new ResponseDTO("success", "Usuario registrado com sucesso", registrationData);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
