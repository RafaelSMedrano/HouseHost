package com.househost.auth.adapter.out.persistence;

import com.househost.auth.adapter.out.persistence.entity.LoginSecurityJpaEntity;
import com.househost.auth.adapter.out.persistence.entity.LoginSecurityPersistenceMapper;
import com.househost.auth.application.port.out.LoginSecurityPersistencePort;
import com.househost.auth.domain.model.LoginSecurityControl;
import com.househost.auth.domain.model.LoginSecurityScope;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class LoginSecurityPersistenceAdapter implements LoginSecurityPersistencePort {
    private static final int CREATE_RETRIES = 4;
    private final LoginSecurityJpaRepository repository;
    private final TransactionTemplate transactions;

    public LoginSecurityPersistenceAdapter(LoginSecurityJpaRepository repository,
                                          PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Override
    public Optional<LoginSecurityControl> find(LoginSecurityScope scope, String scopeKey) {
        return repository.findByScopeAndScopeKey(scope, scopeKey).map(LoginSecurityPersistenceMapper::toDomain);
    }

    @Override
    public LoginSecurityControl mutate(LoginSecurityScope scope, String scopeKey,
                                      Consumer<LoginSecurityControl> mutation) {
        for (int attempt = 1; attempt <= CREATE_RETRIES; attempt++) {
            try {
                LoginSecurityControl result = transactions.execute(status -> {
                    LoginSecurityControl control = repository.findForUpdate(scope, scopeKey)
                            .map(LoginSecurityPersistenceMapper::toDomain)
                            .orElseGet(() -> new LoginSecurityControl(scope, scopeKey));
                    mutation.accept(control);
                    LoginSecurityJpaEntity saved = repository.saveAndFlush(LoginSecurityPersistenceMapper.toEntity(control));
                    return LoginSecurityPersistenceMapper.toDomain(saved);
                });
                if (result == null) throw new IllegalStateException("Login-security transaction returned no state");
                return result;
            } catch (DataIntegrityViolationException exception) {
                if (attempt == CREATE_RETRIES) throw exception;
                Thread.onSpinWait();
            }
        }
        throw new IllegalStateException("Unable to persist login-security state");
    }

    @Override
    public void clear(LoginSecurityScope scope, String scopeKey, Instant now) {
        transactions.executeWithoutResult(status -> repository.findForUpdate(scope, scopeKey).ifPresent(entity -> {
            LoginSecurityControl control = LoginSecurityPersistenceMapper.toDomain(entity);
            control.clear(now);
            repository.save(LoginSecurityPersistenceMapper.toEntity(control));
        }));
    }

    @Override
    public int deleteStale(Instant cutoff, Instant now) {
        Integer deleted = transactions.execute(status -> repository.deleteStale(cutoff, now));
        return deleted == null ? 0 : deleted;
    }
}
