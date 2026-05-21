package com.shop.security;

/**
 * Checks whether a plaintext password has appeared in known data breaches
 * using the Have I Been Pwned (HIBP) k-anonymity API (SEC-PWD-002 / CPA-16).
 */
public interface PasswordBreachService {

    /**
     * Returns {@code true} when the given plaintext password is found in the HIBP database.
     * Only the first 5 characters of the SHA-1 hash are transmitted to the HIBP API;
     * the full hash never leaves the system.
     *
     * @param plainPassword the candidate password to check
     * @return {@code true} if the password is compromised, {@code false} otherwise
     */
    boolean isCompromised(String plainPassword);
}
