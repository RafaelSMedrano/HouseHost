package com.househost.auth.adapter.out.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginSecurityConfig {
    @Bean
    public Clock loginSecurityClock() {
        return Clock.systemUTC();
    }
}
