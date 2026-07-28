package com.househost.auth.application.port.out;

public interface LoginSecurityKeyPort {
    String forEmail(String normalizedEmail);
    String forIp(String normalizedIpAddress);
    String forPair(String normalizedEmail, String normalizedIpAddress);
}
