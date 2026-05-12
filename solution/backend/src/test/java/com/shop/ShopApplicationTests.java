package com.shop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Verifies that the application context loads. */
@SpringBootTest
@ActiveProfiles("test")
class ShopApplicationTests {

    /** Checks that the Spring context starts without errors. */
    @Test
    void contextLoads() {
    }
}
