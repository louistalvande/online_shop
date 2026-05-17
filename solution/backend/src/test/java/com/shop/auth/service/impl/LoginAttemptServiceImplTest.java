package com.shop.auth.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

/** Unit tests for {@link LoginAttemptServiceImpl}. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoginAttemptServiceImplTest {

    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> valueOps;

    LoginAttemptServiceImpl service;

    private static final String EMAIL = "user@example.com";
    private static final String KEY   = "login_attempt:" + EMAIL;

    @BeforeEach
    void setUp() {
        given(redis.opsForValue()).willReturn(valueOps);
        service = new LoginAttemptServiceImpl(redis, 5, 900L);
    }

    /** First failure must increment the counter and set the TTL. */
    @Test
    void recordFailure_firstAttempt_setsExpiry() {
        given(valueOps.increment(KEY)).willReturn(1L);

        service.recordFailure(EMAIL);

        then(valueOps).should().increment(KEY);
        then(redis).should().expire(KEY, Duration.ofSeconds(900));
    }

    /** Subsequent failures increment without resetting TTL. */
    @Test
    void recordFailure_subsequentAttempts_doesNotResetExpiry() {
        given(valueOps.increment(KEY)).willReturn(3L);

        service.recordFailure(EMAIL);

        then(valueOps).should().increment(KEY);
        then(redis).should(never()).expire(any(), any());
    }

    /** Successful login must delete the counter key. */
    @Test
    void recordSuccess_deletesKey() {
        service.recordSuccess(EMAIL);

        then(redis).should().delete(KEY);
    }

    /** isBlocked returns false when no counter entry exists. */
    @Test
    void isBlocked_noEntry_returnsFalse() {
        given(valueOps.get(KEY)).willReturn(null);

        assertThat(service.isBlocked(EMAIL)).isFalse();
    }

    /** isBlocked returns false when count is below the threshold. */
    @Test
    void isBlocked_belowThreshold_returnsFalse() {
        given(valueOps.get(KEY)).willReturn("4");

        assertThat(service.isBlocked(EMAIL)).isFalse();
    }

    /** isBlocked returns true when count reaches maxAttempts. */
    @Test
    void isBlocked_atThreshold_returnsTrue() {
        given(valueOps.get(KEY)).willReturn("5");

        assertThat(service.isBlocked(EMAIL)).isTrue();
    }

    /** isBlocked returns true when count exceeds maxAttempts. */
    @Test
    void isBlocked_aboveThreshold_returnsTrue() {
        given(valueOps.get(KEY)).willReturn("9");

        assertThat(service.isBlocked(EMAIL)).isTrue();
    }
}
