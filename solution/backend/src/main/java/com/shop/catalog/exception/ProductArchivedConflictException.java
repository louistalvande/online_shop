package com.shop.catalog.exception;

import java.util.UUID;

/** Thrown when a product cannot be archived because it is referenced in an active order (US-CAT-03). */
public class ProductArchivedConflictException extends RuntimeException {

    /**
     * Constructs the exception for the given product UUID.
     *
     * @param id the product UUID that cannot be archived
     */
    public ProductArchivedConflictException(UUID id) {
        super("Product " + id + " cannot be archived: referenced in one or more active orders");
    }
}
