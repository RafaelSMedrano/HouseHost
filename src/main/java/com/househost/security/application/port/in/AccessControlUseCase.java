package com.househost.security.application.port.in;

public interface AccessControlUseCase {

    boolean canManageUsers();

    void requireSelfOrUserAdministrator(Long userId);
}
