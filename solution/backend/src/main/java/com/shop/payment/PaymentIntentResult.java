package com.shop.payment;

/** Holds the Stripe PaymentIntent identifiers returned to the caller after creation. */
public class PaymentIntentResult {

    /** Stripe PaymentIntent ID (e.g. {@code pi_3OxQ...}). */
    private final String paymentIntentId;

    /**
     * Client secret forwarded to the frontend so it can confirm the payment with Stripe.js
     * without this backend ever seeing the card data (PCI-DSS).
     */
    private final String clientSecret;

    /**
     * Constructs the result.
     *
     * @param paymentIntentId the Stripe PaymentIntent ID
     * @param clientSecret    the client secret for frontend confirmation
     */
    public PaymentIntentResult(String paymentIntentId, String clientSecret) {
        this.paymentIntentId = paymentIntentId;
        this.clientSecret = clientSecret;
    }

    /** @return the Stripe PaymentIntent ID */
    public String getPaymentIntentId() { return paymentIntentId; }

    /** @return the client secret for Stripe.js frontend confirmation */
    public String getClientSecret() { return clientSecret; }
}
