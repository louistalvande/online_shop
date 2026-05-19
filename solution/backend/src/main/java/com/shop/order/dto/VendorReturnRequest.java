package com.shop.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request body for vendor-initiated post-shipment cancellation (US-CAN-03, US-CAN-04). */
public class VendorReturnRequest {

    @Schema(description = "Buyer IBAN for wire transfer refund — required when the order was paid by WIRE_TRANSFER",
            example = "FR7630006000011234567890189")
    private String buyerIban;

    /** @return the buyer's IBAN for refund, or {@code null} for card orders */
    public String getBuyerIban() { return buyerIban; }

    /** @param buyerIban the buyer's IBAN */
    public void setBuyerIban(String buyerIban) { this.buyerIban = buyerIban; }
}
