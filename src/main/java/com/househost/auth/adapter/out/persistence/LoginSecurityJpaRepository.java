package com.househost.auth.adapter.out.persistence;

import com.househost.auth.adapter.out.persistence.entity.LoginSecurityJpaEntity;
import com.househost.auth.domain.model.LoginSecurityScope;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginSecurityJpaRepository extends JpaRepository<LoginSecurityJpaEntity, Long> {
    Optional<LoginSecurityJpaEntity> findByScopeAndScopeKey(LoginSecurityScope scope, String scopeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select control from LoginSecurityJpaEntity control where control.scope = :scope and control.scopeKey = :scopeKey")
    Optional<LoginSecurityJpaEntity> findForUpdate(@Param("scope") LoginSecurityScope scope,
                                                   @Param("scopeKey") String scopeKey);

    @Modifying
    @Query("delete from LoginSecurityJpaEntity control where control.updatedAt < :cutoff " +
            "and (control.blockedUntil is null or control.blockedUntil <= :now)")
    int deleteStale(@Param("cutoff") Instant cutoff, @Param("now") Instant now);
}
