package com.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entry point of the Shop API. */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ShopApplication {

    /**
     * Starts the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
