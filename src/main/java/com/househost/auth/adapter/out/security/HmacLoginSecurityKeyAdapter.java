package com.househost.auth.adapter.out.security;

import com.househost.auth.application.port.out.LoginSecurityKeyPort;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmacLoginSecurityKeyAdapter implements LoginSecurityKeyPort {
    private final byte[] secret;

    public HmacLoginSecurityKeyAdapter(@Value("${househost.login-protection.hmac-secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("A dedicated login-protection HMAC secret is required");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override public String forEmail(String normalizedEmail) { return derive("email\u0000" + normalizedEmail); }
    @Override public String forIp(String normalizedIpAddress) { return derive("ip\u0000" + normalizedIpAddress); }
    @Override public String forPair(String normalizedEmail, String normalizedIpAddress) {
        return derive("pair\u0000" + normalizedEmail + "\u0000" + normalizedIpAddress);
    }

    private String derive(String material) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(material.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to derive login-protection key", exception);
        }
    }
}
