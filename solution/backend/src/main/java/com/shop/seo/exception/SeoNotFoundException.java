package com.shop.seo.exception;

import java.util.UUID;

/** Thrown when a product SEO override is not found. */
public class SeoNotFoundException extends RuntimeException {

    /**
     * Creates the exception for a missing product SEO override.
     *
     * @param productId the product UUID that has no override
     */
    public SeoNotFoundException(UUID productId) {
        super("No SEO override found for product: " + productId);
    }
}
