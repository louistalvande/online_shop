package com.shop.catalog.exception;

import java.util.UUID;

/** Thrown when a back-in-stock subscription cannot be found for the given buyer and product. */
public class StockSubscriptionNotFoundException extends RuntimeException {

    /**
     * @param productId the UUID of the product whose subscription was not found
     */
    public StockSubscriptionNotFoundException(UUID productId) {
        super("No active stock subscription found for product " + productId);
    }
}
