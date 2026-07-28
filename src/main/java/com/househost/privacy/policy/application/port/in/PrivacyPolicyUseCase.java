package com.househost.privacy.policy.application.port.in;

import com.househost.privacy.policy.application.dto.PrivacyPolicyRequestDTO;
import com.househost.privacy.policy.application.dto.PrivacyPolicyResponseDTO;
import java.util.List;

public interface PrivacyPolicyUseCase {
    PrivacyPolicyResponseDTO createDraft(PrivacyPolicyRequestDTO request);

    PrivacyPolicyResponseDTO updateDraft(Long id, PrivacyPolicyRequestDTO request);

    List<PrivacyPolicyResponseDTO> findAll();

    PrivacyPolicyResponseDTO findById(Long id);

    PrivacyPolicyResponseDTO publish(Long id, String authenticatedEmail);
}
