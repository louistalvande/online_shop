package com.shop.catalog.exception;

import java.util.UUID;

/** Thrown when a buyer tries to subscribe to a back-in-stock alert for a product that is already in stock. */
public class ProductInStockException extends RuntimeException {

    /**
     * @param productId the UUID of the product that is already in stock
     */
    public ProductInStockException(UUID productId) {
        super("Product " + productId + " is already in stock — no alert needed");
    }
}
