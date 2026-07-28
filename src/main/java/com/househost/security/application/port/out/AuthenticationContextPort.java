package com.househost.security.application.port.out;

import java.util.Optional;

public interface AuthenticationContextPort {

    Optional<String> currentUsername();
}
