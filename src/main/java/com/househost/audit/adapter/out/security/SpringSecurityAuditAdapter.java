package com.househost.audit.adapter.out.security;

import com.househost.audit.application.port.out.AuditActorContextPort;
import com.househost.audit.domain.model.AuditActor;
import com.househost.audit.domain.model.AuditEventContext;
import com.househost.security.application.port.out.SecurityIdentityPort;
import com.househost.security.domain.model.SecurityIdentity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SpringSecurityAuditAdapter implements AuditActorContextPort {
    private final SecurityIdentityPort securityIdentityPort;

    public SpringSecurityAuditAdapter(SecurityIdentityPort securityIdentityPort) {
        this.securityIdentityPort = securityIdentityPort;
    }

    public AuditActor currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return new AuditActor("SYSTEM", null, "SYSTEM", currentRequestContext());
        }
        SecurityIdentity identity = securityIdentityPort.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado nao encontrado."));
        return new AuditActor("ADMIN_USER", identity.id(), identity.username(), currentRequestContext());
    }

    public AuditEventContext currentRequestContext() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return null;
        HttpServletRequest request = attributes.getRequest();
        return new AuditEventContext(resolveIpAddress(request), request.getHeader("User-Agent"));
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) return forwardedFor.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp.trim();
    }
}
