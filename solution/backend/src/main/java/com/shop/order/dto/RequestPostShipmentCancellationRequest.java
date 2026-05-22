package com.shop.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for a buyer post-shipment cancellation request (US-CAN-06). */
public class RequestPostShipmentCancellationRequest {

    @NotBlank
    @Size(max = 500)
    @Schema(description = "Mandatory reason for requesting the cancellation",
            example = "Item no longer needed",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "Buyer IBAN for wire refund — required when the order was paid by WIRE_TRANSFER",
            example = "FR7630006000011234567890189")
    private String buyerIban;

    /** @return the reason for the cancellation request */
    public String getReason() { return reason; }

    /** @param reason the reason for the cancellation request */
    public void setReason(String reason) { this.reason = reason; }

    /** @return the buyer's IBAN for refund, or {@code null} for card orders */
    public String getBuyerIban() { return buyerIban; }

    /** @param buyerIban the buyer's IBAN */
    public void setBuyerIban(String buyerIban) { this.buyerIban = buyerIban; }
}
