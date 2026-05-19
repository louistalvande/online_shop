package com.shop.order.entity;

/** Lifecycle states of a buyer order. */
public enum OrderStatus {
    /** Card payment intent created; waiting for the frontend to confirm with Stripe.js (US-ORD-03). */
    PAYMENT_PENDING_CARD,
    /** Wire transfer created, waiting for vendor to confirm receipt (US-ORD-04). */
    PAYMENT_PENDING_WIRE,
    /** Payment confirmed; waiting for vendor to start processing (US-ORD-03, US-VND-02). */
    AWAITING_PROCESSING,
    /** Vendor has accepted and is preparing the shipment (US-VND-01). */
    IN_PREPARATION,
    /** Order shipped; tracking number available (US-EXP-01). */
    SHIPPED,
    /** Order fully delivered and closed. */
    DELIVERED,
    /** Order cancelled (by buyer or vendor). */
    CANCELLED,
    /** Cancelled after shipment; waiting for the buyer to return the parcel (US-CAN-03). */
    PENDING_RETURN,
    /** Wire refund emitted by vendor, awaiting settlement (US-CAN-02, US-CAN-04, US-CLM-02). */
    WIRE_REFUND_IN_PROGRESS
}
