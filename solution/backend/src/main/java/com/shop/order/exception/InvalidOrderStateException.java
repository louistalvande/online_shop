package com.shop.order.exception;

import com.shop.order.entity.OrderStatus;
import java.util.UUID;

/** Thrown when an operation is not allowed in the order's current status. */
public class InvalidOrderStateException extends RuntimeException {

    /**
     * Constructs the exception.
     *
     * @param orderId       the order UUID
     * @param currentStatus the order's current status
     */
    public InvalidOrderStateException(UUID orderId, OrderStatus currentStatus) {
        super("Operation not allowed for order " + orderId + " in status " + currentStatus);
    }
}
