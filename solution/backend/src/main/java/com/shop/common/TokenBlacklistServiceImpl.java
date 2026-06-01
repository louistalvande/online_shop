package com.shop.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/** Redis-backed implementation of {@link TokenBlacklistService}. */
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String KEY_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redis;

    /** @param redis the Redis template used for blacklist storage */
    public TokenBlacklistServiceImpl(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** {@inheritDoc} */
    @Override
    public void blacklist(String token, long remainingMs) {
        if (remainingMs <= 0) return;
        redis.opsForValue().set(KEY_PREFIX + token, "1", remainingMs, TimeUnit.MILLISECONDS);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redis.hasKey(KEY_PREFIX + token));
    }
}
