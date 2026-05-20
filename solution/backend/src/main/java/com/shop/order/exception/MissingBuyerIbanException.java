package com.shop.order.exception;

import java.util.UUID;

/** Thrown when a wire transfer order is cancelled without providing a refund IBAN (US-CAN-01). */
public class MissingBuyerIbanException extends RuntimeException {

    /**
     * @param orderId the order UUID whose wire refund IBAN is missing
     */
    public MissingBuyerIbanException(UUID orderId) {
        super("Buyer IBAN is required to cancel wire transfer order " + orderId);
    }
}
