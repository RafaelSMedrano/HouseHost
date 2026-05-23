package com.househost.auth.controller;

import com.househost.auth.dto.LoginRequestDTO;
import com.househost.auth.dto.LoginResponseDTO;
import com.househost.auth.dto.RegistrationRequestDTO;
import com.househost.auth.dto.RegistrationResponseDTO;
import com.househost.auth.dto.UserPhotoRequestDTO;
import com.househost.auth.dto.UserProfileUpdateRequestDTO;
import com.househost.auth.service.AuthService;
import com.househost.shared.dto.ResponseDTO;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseDTO login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO data = authService.login(request);
        return new ResponseDTO("success", "Login realizado com sucesso", data);
    }

    @PostMapping("/registration")
    public ResponseDTO registration(@RequestBody RegistrationRequestDTO request) {
        RegistrationResponseDTO data = authService.registration(request);
        return new ResponseDTO("success", "Usuario registrado com sucesso", data);
    }

    @GetMapping("/users/quick-access")
    public ResponseDTO quickAccessUsers() {
        List<LoginResponseDTO> data = authService.quickAccessUsers();
        return new ResponseDTO("success", "Usuarios encontrados com sucesso", data);
    }

    @PutMapping("/users/{id}/photo")
    public ResponseDTO updateUserPhoto(@PathVariable Long id, @RequestBody UserPhotoRequestDTO request) {
        LoginResponseDTO data = authService.updateUserPhoto(id, request);
        return new ResponseDTO("success", "Foto atualizada com sucesso", data);
    }

    @PutMapping("/users/{id}")
    public ResponseDTO updateUserProfile(@PathVariable Long id, @RequestBody UserProfileUpdateRequestDTO request) {
        LoginResponseDTO data = authService.updateUserProfile(id, request);
        return new ResponseDTO("success", "Perfil atualizado com sucesso", data);
    }
}
