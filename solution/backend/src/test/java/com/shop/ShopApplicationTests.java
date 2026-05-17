package com.shop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Verifies that the application context loads. */
@SpringBootTest
@ActiveProfiles("test")
class ShopApplicationTests {

    /** Mock Redis — excluded by application-test.yml but required by LoginAttemptServiceImpl. */
    @MockBean
    StringRedisTemplate stringRedisTemplate;

    /** Checks that the Spring context starts without errors. */
    @Test
    void contextLoads() {
    }
}
