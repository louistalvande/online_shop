package com.shop.order.entity;

/** Payment method chosen by the buyer at checkout (US-ORD-03, US-ORD-04). */
public enum PaymentMethod {
    /** Stripe card payment — immediate authorisation. */
    CARD,
    /** Bank wire transfer — order held until vendor confirms receipt. */
    WIRE_TRANSFER
}
