package com.househost.auth.application.records;

public record LoginRequestContextRecord(String ipAddress, String userAgent) {
    public LoginRequestContextRecord {
        if (ipAddress == null || ipAddress.isBlank()) {
            throw new IllegalArgumentException("Client IP address is required");
        }
        userAgent = userAgent == null || userAgent.isBlank() ? null : userAgent.trim();
    }
}
