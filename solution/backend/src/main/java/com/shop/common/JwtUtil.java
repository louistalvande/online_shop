package com.shop.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/** Utility for generating and validating JWT tokens. */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    /**
     * Constructs the utility, deriving the signing key from the configured base64 secret.
     *
     * @param secret       base64-encoded HMAC-SHA secret (min 32 bytes decoded)
     * @param expirationMs token validity in milliseconds
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a signed JWT for the given subject and role.
     *
     * @param email the account email used as the token subject
     * @param role  the account role stored as a claim (e.g. "ADMIN")
     * @return a compact signed JWT string
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the subject (email) from a valid token.
     *
     * @param token the JWT string
     * @return the email stored as subject
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the role claim from a valid token.
     *
     * @param token the JWT string
     * @return the role value (e.g. "ADMIN")
     */
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Returns true if the token signature and expiration are valid.
     *
     * @param token the JWT string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns the expiration timestamp of the given token.
     *
     * @param token the JWT string
     * @return the expiration {@link Date}
     */
    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    /**
     * Returns the configured token validity duration in milliseconds.
     *
     * @return token TTL in ms
     */
    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
