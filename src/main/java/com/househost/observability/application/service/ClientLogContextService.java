package com.househost.observability.application.service;

import com.househost.observability.application.records.ClientLogRequestContextRecord;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ClientLogContextService {

    public ClientLogRequestContextRecord create(
            Authentication authentication,
            String directRemoteAddress,
            String requestCorrelationId
    ) {
        String actor = authentication == null ? "unknown" : authentication.getName();
        String origin = directRemoteAddress == null || directRemoteAddress.isBlank()
                ? "unknown"
                : directRemoteAddress;
        return new ClientLogRequestContextRecord(
                reference("actor", actor),
                reference("origin", origin),
                requestCorrelationId,
                Instant.now()
        );
    }

    private String reference(String prefix, String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return prefix + "-" + java.util.HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
