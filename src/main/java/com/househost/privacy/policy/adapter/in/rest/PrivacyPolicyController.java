package com.househost.privacy.policy.adapter.in.rest;

import com.househost.privacy.policy.application.dto.PrivacyPolicyRequestDTO;
import com.househost.privacy.policy.application.port.in.PrivacyPolicyUseCase;
import com.househost.shared.dto.ResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/privacy-policies")
public class PrivacyPolicyController {
    private final PrivacyPolicyUseCase useCase;

    public PrivacyPolicyController(PrivacyPolicyUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseDTO createDraft(@RequestBody PrivacyPolicyRequestDTO request) {
        return new ResponseDTO(
                "success",
                "Politica de privacidade criada como rascunho.",
                useCase.createDraft(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseDTO updateDraft(
            @PathVariable Long id,
            @RequestBody PrivacyPolicyRequestDTO request
    ) {
        return new ResponseDTO(
                "success",
                "Rascunho da politica de privacidade atualizado.",
                useCase.updateDraft(id, request)
        );
    }

    @PostMapping("/{id}/publish")
    public ResponseDTO publish(@PathVariable Long id, Authentication authentication) {
        String authenticatedEmail = authentication == null ? null : authentication.getName();
        return new ResponseDTO(
                "success",
                "Politica de privacidade publicada.",
                useCase.publish(id, authenticatedEmail)
        );
    }

    @GetMapping
    public ResponseDTO findAll() {
        return new ResponseDTO(
                "success",
                "Politicas de privacidade encontradas.",
                useCase.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return new ResponseDTO(
                "success",
                "Politica de privacidade encontrada.",
                useCase.findById(id)
        );
    }
}
