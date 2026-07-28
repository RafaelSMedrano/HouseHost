package com.househost.auth.application.config;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "househost.login-protection")
public class LoginSecurityPolicyProperties {
    private final ScopePolicy pair = new ScopePolicy(10, Duration.ofMinutes(5), Duration.ofMinutes(15));
    private final ScopePolicy ip = new ScopePolicy(30, Duration.ofMinutes(5), Duration.ofMinutes(30));
    private final ScopePolicy account = new ScopePolicy(20, Duration.ofMinutes(10), Duration.ofMinutes(15));
    private Duration retention = Duration.ofDays(30);

    @PostConstruct
    void validate() {
        pair.validate("pair");
        ip.validate("ip");
        account.validate("account");
        requirePositive(retention, "retention");
    }

    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalStateException("househost.login-protection." + name + " must be positive");
        }
    }

    public ScopePolicy getPair() { return pair; }
    public ScopePolicy getIp() { return ip; }
    public ScopePolicy getAccount() { return account; }
    public Duration getRetention() { return retention; }
    public void setRetention(Duration retention) { this.retention = retention; }

    public static class ScopePolicy {
        private int maxFailures;
        private Duration window;
        private Duration block;

        public ScopePolicy() { }
        ScopePolicy(int maxFailures, Duration window, Duration block) {
            this.maxFailures = maxFailures;
            this.window = window;
            this.block = block;
        }
        void validate(String name) {
            if (maxFailures <= 0) {
                throw new IllegalStateException("househost.login-protection." + name + ".max-failures must be positive");
            }
            requirePositive(window, name + ".window");
            requirePositive(block, name + ".block");
        }
        public int getMaxFailures() { return maxFailures; }
        public void setMaxFailures(int maxFailures) { this.maxFailures = maxFailures; }
        public Duration getWindow() { return window; }
        public void setWindow(Duration window) { this.window = window; }
        public Duration getBlock() { return block; }
        public void setBlock(Duration block) { this.block = block; }
    }
}
