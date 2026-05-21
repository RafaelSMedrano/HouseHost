package com.househost.auth.controller;

import com.househost.auth.dto.LoginRequestDTO;
import com.househost.auth.dto.RegistrationRequestDTO;
import com.househost.auth.dto.UserPhotoRequestDTO;
import com.househost.auth.dto.UserProfileUpdateRequestDTO;
import com.househost.auth.service.AuthService;
import com.househost.shared.dto.ResponseDTO;
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
        return authService.login(request);
    }

    @PostMapping("/registration")
    public ResponseDTO registration(@RequestBody RegistrationRequestDTO request) {
        return authService.registration(request);
    }

    @GetMapping("/users/quick-access")
    public ResponseDTO quickAccessUsers() {
        return authService.quickAccessUsers();
    }

    @PutMapping("/users/{id}/photo")
    public ResponseDTO updateUserPhoto(@PathVariable Long id, @RequestBody UserPhotoRequestDTO request) {
        return authService.updateUserPhoto(id, request);
    }

    @PutMapping("/users/{id}")
    public ResponseDTO updateUserProfile(@PathVariable Long id, @RequestBody UserProfileUpdateRequestDTO request) {
        return authService.updateUserProfile(id, request);
    }
}
