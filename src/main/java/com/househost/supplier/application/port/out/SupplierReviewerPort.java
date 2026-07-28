package com.househost.supplier.application.port.out;

public interface SupplierReviewerPort {
    Long findReviewerIdByEmail(String email);
}
