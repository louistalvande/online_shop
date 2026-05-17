package com.shop.auth.service;

/**
 * Tracks failed login attempts and enforces temporary account lockout (SEC-AUTH-003 / CPA-11).
 * Backed by Redis for distributed, TTL-based counters.
 */
public interface LoginAttemptService {

    /**
     * Records a failed login attempt for the given email.
     * Increments the counter and sets a TTL on first attempt.
     *
     * @param email the account email address
     */
    void recordFailure(String email);

    /**
     * Clears the failure counter for the given email after a successful login.
     *
     * @param email the account email address
     */
    void recordSuccess(String email);

    /**
     * Returns {@code true} when the account is currently locked out.
     *
     * @param email the account email address
     * @return {@code true} if the failure counter has reached the configured maximum
     */
    boolean isBlocked(String email);
}
