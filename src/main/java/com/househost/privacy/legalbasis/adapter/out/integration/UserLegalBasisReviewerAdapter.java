package com.househost.privacy.legalbasis.adapter.out.integration;

import com.househost.auth.application.port.in.UserUseCase;
import com.househost.privacy.legalbasis.application.port.out.LegalBasisReviewerPort;
import org.springframework.stereotype.Component;

@Component
public class UserLegalBasisReviewerAdapter implements LegalBasisReviewerPort {
    private final UserUseCase userUseCase;

    public UserLegalBasisReviewerAdapter(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @Override
    public Long findReviewerIdByEmail(String email) {
        return userUseCase.findByEmail(email).getId();
    }
}
