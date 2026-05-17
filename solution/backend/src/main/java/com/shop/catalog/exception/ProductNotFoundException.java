package com.shop.catalog.exception;

import java.util.UUID;

/** Thrown when a product does not exist or does not belong to the requesting vendor. */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Constructs the exception for the given product UUID.
     *
     * @param id the product UUID that was not found
     */
    public ProductNotFoundException(UUID id) {
        super("Product not found: " + id);
    }
}
