package com.resumelint.security;

import com.resumelint.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Mirrors the Node backend's {@code signToken} / {@code jwt.verify} pair in
 * {@code middlewares/auth.ts}: HS256, 7 day expiry, payload of
 * {@code { userId, email }}.
 */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String signToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpirationMs());
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Verifies and decodes the token.
     *
     * @throws JwtException if the token is missing, malformed, expired, or
     *                       has an invalid signature.
     */
    public AuthPayload verifyToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object rawUserId = claims.get("userId");
        Long userId = (rawUserId instanceof Number number) ? number.longValue() : null;
        String email = claims.get("email", String.class);
        return new AuthPayload(userId, email);
    }
}
