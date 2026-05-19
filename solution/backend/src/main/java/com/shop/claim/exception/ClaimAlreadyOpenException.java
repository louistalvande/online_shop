package com.shop.claim.exception;

import java.util.UUID;

/** Thrown when the buyer attempts to open a second claim on an order that already has one open. */
public class ClaimAlreadyOpenException extends RuntimeException {

    /**
     * @param orderId the UUID of the order that already has an open claim
     */
    public ClaimAlreadyOpenException(UUID orderId) {
        super("An open claim already exists for order " + orderId);
    }
}
