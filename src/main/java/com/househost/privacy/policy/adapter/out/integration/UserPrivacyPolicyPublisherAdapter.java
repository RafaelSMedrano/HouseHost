package com.househost.privacy.policy.adapter.out.integration;

import com.househost.auth.application.dto.UserResponseDTO;
import com.househost.auth.application.port.in.UserUseCase;
import com.househost.auth.domain.model.UserRole;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyPublisherPort;
import com.househost.shared.exception.PrivacyException;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class UserPrivacyPolicyPublisherAdapter implements PrivacyPolicyPublisherPort {
    private static final Set<UserRole> PUBLISHER_ROLE_SET = Set.of(
            UserRole.CEO,
            UserRole.CTO,
            UserRole.ADMIN
    );

    private final UserUseCase userUseCase;

    public UserPrivacyPolicyPublisherAdapter(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @Override
    public Long findPublisherIdByEmail(String email) {
        return userUseCase.findByEmail(email).getId();
    }

    @Override
    public Long findInitialPublisherId() {
        return userUseCase.quickAccessUsers().stream()
                .filter(user -> PUBLISHER_ROLE_SET.contains(user.getRole()))
                .map(UserResponseDTO::getId)
                .min(Long::compareTo)
                .orElseThrow(() -> new PrivacyException(
                        "Nenhum usuario administrativo disponivel para publicar a politica inicial."
                ));
    }
}
