package com.shop.order.exception;

/** Thrown when a Stripe card authorisation is declined or fails (US-ORD-03). */
public class PaymentFailedException extends RuntimeException {

    /**
     * Constructs the exception with a Stripe-provided decline reason.
     *
     * @param reason the decline reason returned by Stripe
     */
    public PaymentFailedException(String reason) {
        super("Card payment failed: " + reason);
    }
}
