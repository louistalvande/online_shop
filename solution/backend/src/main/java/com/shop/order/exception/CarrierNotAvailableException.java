package com.shop.order.exception;

import java.util.UUID;

/** Thrown when the selected carrier is inactive or does not cover the delivery country (US-ORD-02). */
public class CarrierNotAvailableException extends RuntimeException {

    /**
     * Constructs the exception.
     *
     * @param carrierId the carrier UUID that was rejected
     */
    public CarrierNotAvailableException(UUID carrierId) {
        super("Carrier not available for the selected delivery country: " + carrierId);
    }
}
