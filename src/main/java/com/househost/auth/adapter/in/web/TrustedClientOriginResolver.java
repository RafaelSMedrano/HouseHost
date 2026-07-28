package com.househost.auth.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TrustedClientOriginResolver {
    private final List<String> trustedProxies;

    public TrustedClientOriginResolver(
            @Value("${househost.login-protection.trusted-proxies:}") String trustedProxies) {
        this.trustedProxies = Arrays.stream(trustedProxies.split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).toList();
    }

    public String resolve(HttpServletRequest request) {
        String directPeer = normalizeAddress(request.getRemoteAddr());
        if (!isTrusted(directPeer)) {
            return directPeer;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            String realIp = request.getHeader("X-Real-IP");
            return validAddress(realIp).orElse(directPeer);
        }
        String first = forwarded.split(",", -1)[0].trim();
        return validAddress(first).orElse(directPeer);
    }

    private boolean isTrusted(String address) {
        return trustedProxies.stream().anyMatch(configured -> matches(address, configured));
    }

    private boolean matches(String address, String configured) {
        if (!configured.contains("/")) return address.equals(normalizeAddress(configured));
        String[] parts = configured.split("/", -1);
        if (parts.length != 2) return false;
        try {
            byte[] candidate = InetAddress.getByName(address).getAddress();
            byte[] network = InetAddress.getByName(parts[0]).getAddress();
            int prefix = Integer.parseInt(parts[1]);
            if (candidate.length != network.length || prefix < 0 || prefix > candidate.length * 8) return false;
            for (int bit = 0; bit < prefix; bit++) {
                int mask = 1 << (7 - bit % 8);
                if ((candidate[bit / 8] & mask) != (network[bit / 8] & mask)) return false;
            }
            return true;
        } catch (RuntimeException | UnknownHostException exception) {
            return false;
        }
    }

    private java.util.Optional<String> validAddress(String value) {
        if (value == null || value.isBlank()) return java.util.Optional.empty();
        String candidate = value.trim();
        if (!isIpLiteral(candidate)) return java.util.Optional.empty();
        try {
            return java.util.Optional.of(InetAddress.getByName(candidate).getHostAddress());
        } catch (UnknownHostException exception) {
            return java.util.Optional.empty();
        }
    }

    private boolean isIpLiteral(String value) {
        if (value.indexOf(':') >= 0) {
            return value.matches("[0-9a-fA-F:.%]+") && value.chars().filter(character -> character == ':').count() >= 2;
        }
        String[] octets = value.split("\\.", -1);
        if (octets.length != 4) return false;
        for (String octet : octets) {
            if (!octet.matches("[0-9]{1,3}")) return false;
            int number = Integer.parseInt(octet);
            if (number < 0 || number > 255) return false;
        }
        return true;
    }

    private String normalizeAddress(String value) {
        return validAddress(value).orElse("unknown");
    }
}
