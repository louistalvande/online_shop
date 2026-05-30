package com.shop.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Request body for subscribing to a back-in-stock alert (US-SHP-03). */
public class StockSubscriptionRequest {

    /** UUID of the product to watch. */
    @Schema(description = "UUID of the out-of-stock product to subscribe to")
    @NotNull
    private UUID productId;

    /** @return the product UUID */
    public UUID getProductId() { return productId; }

    /** @param productId the product UUID to set */
    public void setProductId(UUID productId) { this.productId = productId; }
}
