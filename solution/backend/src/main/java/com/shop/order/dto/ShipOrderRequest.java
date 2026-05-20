package com.shop.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request body for shipment declaration (US-EXP-01). */
public class ShipOrderRequest {

    @NotBlank
    @Schema(description = "Tracking number from the carrier receipt", example = "1Z999AA10123456784")
    private String trackingNumber;

    /** @return the carrier tracking number */
    public String getTrackingNumber() { return trackingNumber; }

    /** @param trackingNumber the carrier tracking number */
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
}
