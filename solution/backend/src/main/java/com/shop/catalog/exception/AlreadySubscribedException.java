package com.shop.catalog.exception;

import java.util.UUID;

/** Thrown when a buyer tries to subscribe to a product they already have an active alert for. */
public class AlreadySubscribedException extends RuntimeException {

    /**
     * @param productId the UUID of the product already subscribed to
     */
    public AlreadySubscribedException(UUID productId) {
        super("Active stock subscription already exists for product " + productId);
    }
}
