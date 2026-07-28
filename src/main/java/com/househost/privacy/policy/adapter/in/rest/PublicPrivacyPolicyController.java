package com.househost.privacy.policy.adapter.in.rest;

import com.househost.privacy.policy.application.port.in.PublicPrivacyPolicyUseCase;
import com.househost.shared.dto.ResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/privacy-policy")
public class PublicPrivacyPolicyController {
    private final PublicPrivacyPolicyUseCase useCase;

    public PublicPrivacyPolicyController(PublicPrivacyPolicyUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseDTO findCurrentPublished() {
        return new ResponseDTO(
                "success",
                "Politica de privacidade vigente encontrada.",
                useCase.findCurrentPublished()
        );
    }
}
