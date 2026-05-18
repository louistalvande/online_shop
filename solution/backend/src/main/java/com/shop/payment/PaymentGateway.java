package com.shop.payment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Abstraction over the card payment provider (Stripe).
 * Implementations must ensure card data is collected exclusively by the
 * payment provider's frontend SDK — never by this backend (PCI-DSS).
 */
public interface PaymentGateway {

    /**
     * Creates a PaymentIntent on Stripe for the given amount and associates it with the order.
     * The returned {@code clientSecret} must be forwarded to the frontend so it can confirm
     * the payment with Stripe.js without exposing card data to this server.
     *
     * @param amountTtc the total amount to authorise in euros (including VAT)
     * @param orderId   the internal order UUID used as Stripe metadata
     * @return a {@link PaymentIntentResult} containing the PaymentIntent ID and client secret
     */
    PaymentIntentResult createPaymentIntent(BigDecimal amountTtc, UUID orderId);

    /**
     * Verifies that a PaymentIntent has been successfully confirmed by the frontend.
     * Called after the client reports success to this backend (US-ORD-03).
     *
     * @param paymentIntentId the Stripe PaymentIntent ID stored on the order
     * @return {@code true} if the PaymentIntent status is {@code succeeded}
     */
    boolean isPaymentSucceeded(String paymentIntentId);

    /**
     * Initiates a full refund for a previously captured PaymentIntent (US-CAN-02, US-CLM-02).
     *
     * @param paymentIntentId the Stripe PaymentIntent ID to refund
     */
    void refund(String paymentIntentId);
}
