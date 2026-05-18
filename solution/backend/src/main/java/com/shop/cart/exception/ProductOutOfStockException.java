package com.shop.cart.exception;

import java.util.UUID;

/** Thrown when a buyer attempts to add an out-of-stock product to the cart (US-CRT-01). */
public class ProductOutOfStockException extends RuntimeException {

    /**
     * Constructs the exception for an out-of-stock product.
     *
     * @param productId the product UUID that has no available stock
     */
    public ProductOutOfStockException(UUID productId) {
        super("Product out of stock: " + productId);
    }
}
