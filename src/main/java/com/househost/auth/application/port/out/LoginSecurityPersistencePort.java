package com.househost.auth.application.port.out;

import com.househost.auth.domain.model.LoginSecurityControl;
import com.househost.auth.domain.model.LoginSecurityScope;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

public interface LoginSecurityPersistencePort {
    Optional<LoginSecurityControl> find(LoginSecurityScope scope, String scopeKey);
    LoginSecurityControl mutate(LoginSecurityScope scope, String scopeKey,
                               Consumer<LoginSecurityControl> mutation);
    void clear(LoginSecurityScope scope, String scopeKey, Instant now);
    int deleteStale(Instant cutoff, Instant now);
}
