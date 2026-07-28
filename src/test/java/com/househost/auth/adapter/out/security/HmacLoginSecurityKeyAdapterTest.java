package com.househost.auth.adapter.out.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HmacLoginSecurityKeyAdapterTest {
    @Test
    void derivesStableFixedLengthKeysWithoutRawMaterial() {
        HmacLoginSecurityKeyAdapter adapter = new HmacLoginSecurityKeyAdapter("dedicated-test-secret");
        String key = adapter.forPair("admin@example.com", "203.0.113.10");
        assertEquals(64, key.length());
        assertEquals(key, adapter.forPair("admin@example.com", "203.0.113.10"));
        assertFalse(key.contains("admin"));
        assertFalse(key.contains("203.0.113.10"));
    }
}
