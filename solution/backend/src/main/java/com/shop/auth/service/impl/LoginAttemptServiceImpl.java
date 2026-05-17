package com.shop.auth.service.impl;

import com.shop.auth.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed implementation of {@link LoginAttemptService}.
 * Uses a single counter key per email with a sliding TTL (CPA-11).
 * Key format: {@code login_attempt:{email}}.
 */
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final String KEY_PREFIX = "login_attempt:";

    private final StringRedisTemplate redis;
    private final int maxAttempts;
    private final Duration lockoutDuration;

    /**
     * Constructs the service with Redis and lockout configuration.
     *
     * @param redis           the Redis string template
     * @param maxAttempts     maximum allowed failures before lockout
     * @param lockoutSeconds  lockout window in seconds
     */
    public LoginAttemptServiceImpl(
            StringRedisTemplate redis,
            @Value("${app.security.max-login-attempts:5}") int maxAttempts,
            @Value("${app.security.lockout-duration-seconds:900}") long lockoutSeconds) {
        this.redis = redis;
        this.maxAttempts = maxAttempts;
        this.lockoutDuration = Duration.ofSeconds(lockoutSeconds);
    }

    /** {@inheritDoc} */
    @Override
    public void recordFailure(String email) {
        String key = KEY_PREFIX + email;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, lockoutDuration);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void recordSuccess(String email) {
        redis.delete(KEY_PREFIX + email);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBlocked(String email) {
        String value = redis.opsForValue().get(KEY_PREFIX + email);
        if (value == null) {
            return false;
        }
        try {
            return Integer.parseInt(value) >= maxAttempts;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
