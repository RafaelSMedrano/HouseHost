package com.househost.supplier.adapter.out.integration;

import com.househost.auth.application.port.in.UserUseCase;
import com.househost.supplier.application.port.out.SupplierReviewerPort;
import org.springframework.stereotype.Component;

@Component
public class SupplierReviewerAdapter implements SupplierReviewerPort {
    private final UserUseCase userUseCase;
    public SupplierReviewerAdapter(UserUseCase userUseCase) { this.userUseCase = userUseCase; }
    public Long findReviewerIdByEmail(String email) { return userUseCase.findByEmail(email).getId(); }
}
