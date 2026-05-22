package com.shop.account.exception;

import java.util.UUID;

/** Thrown when a delivery address is not found or does not belong to the requesting buyer. */
public class DeliveryAddressNotFoundException extends RuntimeException {

    /**
     * Creates the exception for a missing or inaccessible address.
     *
     * @param addressId the UUID of the address that was not found
     */
    public DeliveryAddressNotFoundException(UUID addressId) {
        super("Delivery address not found: " + addressId);
    }
}
