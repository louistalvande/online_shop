package com.shop.cart.exception;

import java.util.UUID;

/** Thrown when a cart item UUID does not exist or does not belong to the requesting buyer. */
public class CartItemNotFoundException extends RuntimeException {

    /**
     * Constructs the exception for a missing or unauthorised cart item.
     *
     * @param itemId the cart item UUID that could not be found
     */
    public CartItemNotFoundException(UUID itemId) {
        super("Cart item not found: " + itemId);
    }
}
