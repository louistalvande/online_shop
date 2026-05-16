package com.shop.carrier.exception;

import java.util.UUID;

/** Thrown when a carrier cannot be found by its UUID. */
public class CarrierNotFoundException extends RuntimeException {

    /**
     * Constructs the exception for the given carrier id.
     *
     * @param id the UUID of the carrier that was not found
     */
    public CarrierNotFoundException(UUID id) {
        super("Carrier not found: " + id);
    }
}
