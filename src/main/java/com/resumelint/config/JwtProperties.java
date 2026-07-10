package com.resumelint.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Binds the {@code app.jwt.*} properties and fails fast at startup if the
 * secret has not been configured, mirroring the Node backend's behavior of
 * throwing immediately when SESSION_SECRET is missing.
 */
@Component
public class JwtProperties {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "SESSION_SECRET environment variable is required but was not set.");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "SESSION_SECRET must be at least 32 characters long for HS256 signing.");
        }
    }

    public String getSecret() {
        return secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
