package com.oscarr.demoappgym.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyAwarePasswordEncoderTests {

    private final LegacyAwarePasswordEncoder passwordEncoder = new LegacyAwarePasswordEncoder();

    @Test
    void matchesLegacyPlainTextPasswords() {
        assertTrue(passwordEncoder.matches("legacy-password", "legacy-password"));
        assertFalse(passwordEncoder.matches("wrong-password", "legacy-password"));
    }

    @Test
    void encodesNewPasswordsWithBcrypt() {
        String encodedPassword = passwordEncoder.encode("new-password");

        assertTrue(encodedPassword.startsWith("$2"));
        assertTrue(passwordEncoder.matches("new-password", encodedPassword));
        assertFalse(passwordEncoder.matches("wrong-password", encodedPassword));
    }
}
