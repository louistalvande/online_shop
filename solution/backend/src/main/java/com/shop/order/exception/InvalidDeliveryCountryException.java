package com.shop.order.exception;

/** Thrown when the buyer selects a delivery country that is not in the Eurozone (CS-04). */
public class InvalidDeliveryCountryException extends RuntimeException {

    /**
     * Constructs the exception for a non-Eurozone country code.
     *
     * @param countryCode the rejected country code
     */
    public InvalidDeliveryCountryException(String countryCode) {
        super("Delivery country not in Eurozone: " + countryCode);
    }
}
