package com.househost.auth.adapter.in.rest;

import com.househost.auth.adapter.in.web.TrustedClientOriginResolver;
import com.househost.auth.application.dto.*;
import com.househost.auth.application.records.LoginRequestContextRecord;
import com.househost.auth.application.port.in.AuthUseCase;
import com.househost.auth.application.port.in.UserUseCase;
import com.househost.shared.dto.ResponseDTO;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthUseCase authUseCase;
    private final UserUseCase userUseCase;
    private final TrustedClientOriginResolver originResolver;

    public AuthController(AuthUseCase authUseCase,
                          UserUseCase userUseCase,
                          TrustedClientOriginResolver originResolver) {
        this.authUseCase = authUseCase;
        this.userUseCase = userUseCase;
        this.originResolver = originResolver;
    }

    @PostMapping("/login")
    public ResponseDTO login(@RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        LoginRequestContextRecord context = new LoginRequestContextRecord(
                originResolver.resolve(httpRequest),
                httpRequest.getHeader("User-Agent")
        );
        LoginResponseDTO data = authUseCase.login(request, context);
        return new ResponseDTO("success", "Login realizado com sucesso", data);
    }

    @PostMapping("/registration")
    public ResponseDTO registration(@RequestBody RegistrationRequestDTO request) {
        RegistrationResponseDTO data = authUseCase.registration(request);
        return new ResponseDTO("success", "Usuario registrado com sucesso", data);
    }

    @GetMapping("/users/quick-access")
    public ResponseDTO quickAccessUsers() {
        List<UserResponseDTO> data = userUseCase.quickAccessUsers();
        return new ResponseDTO("success", "Usuarios encontrados com sucesso", data);
    }

    @PutMapping("/users/{id}/photo")
    public ResponseDTO updateUserPhoto(@PathVariable Long id, @RequestBody UserPhotoRequestDTO request) {
        return new ResponseDTO(
                "success",
                "Foto atualizada com sucesso",
                userUseCase.updateUserPhoto(id, request)
        );
    }

    @PutMapping("/users/{id}")
    public ResponseDTO updateUserProfile(@PathVariable Long id, @RequestBody UserProfileUpdateRequestDTO request) {
        return new ResponseDTO(
                "success",
                "Perfil atualizado com sucesso",
                userUseCase.updateUserProfile(id, request)
        );
    }
}
