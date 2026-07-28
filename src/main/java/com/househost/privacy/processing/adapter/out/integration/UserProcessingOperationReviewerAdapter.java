package com.househost.privacy.processing.adapter.out.integration;

import com.househost.auth.application.port.in.UserUseCase;
import com.househost.privacy.processing.application.port.out.ProcessingOperationReviewerPort;
import org.springframework.stereotype.Component;

@Component
public class UserProcessingOperationReviewerAdapter implements ProcessingOperationReviewerPort {
    private final UserUseCase userUseCase;

    public UserProcessingOperationReviewerAdapter(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @Override
    public Long findReviewerIdByEmail(String email) {
        return userUseCase.findByEmail(email).getId();
    }
}
