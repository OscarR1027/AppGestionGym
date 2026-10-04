package com.oscarr.demoappgym.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class LegacyAwarePasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }

        if (isBcryptHash(encodedPassword)) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }

        return MessageDigest.isEqual(
                rawPassword.toString().getBytes(StandardCharsets.UTF_8),
                encodedPassword.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
