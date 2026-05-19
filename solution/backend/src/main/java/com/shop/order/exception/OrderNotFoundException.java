package com.shop.order.exception;

import java.util.UUID;

/** Thrown when an order UUID is unknown or does not belong to the requesting buyer. */
public class OrderNotFoundException extends RuntimeException {

    /**
     * Constructs the exception for a missing or unauthorised order.
     *
     * @param orderId the order UUID that could not be found
     */
    public OrderNotFoundException(UUID orderId) {
        super("Order not found: " + orderId);
    }
}
