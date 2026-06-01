package com.shop.common;

/**
 * Manages the JWT revocation blacklist used to support explicit logout and the sliding-window
 * token rotation mechanism (US-SEC-01 / FS-S03).
 */
public interface TokenBlacklistService {

    /**
     * Adds a token to the blacklist with a TTL equal to its remaining validity.
     * Tokens that have already expired are ignored.
     *
     * @param token         the JWT string to revoke
     * @param remainingMs   milliseconds until the token naturally expires
     */
    void blacklist(String token, long remainingMs);

    /**
     * Returns {@code true} if the given token has been explicitly revoked.
     *
     * @param token the JWT string to check
     * @return {@code true} if blacklisted, {@code false} otherwise
     */
    boolean isBlacklisted(String token);
}
