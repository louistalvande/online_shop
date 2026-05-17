package com.shop.catalog.dto;

import com.shop.catalog.entity.StockAlert;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/** Read-only representation of a stock alert returned by the API (US-CAT-05). */
public class StockAlertResponse {

    @Schema(description = "Alert UUID")
    private UUID id;

    @Schema(description = "UUID of the product whose stock crossed the threshold")
    private UUID productId;

    @Schema(description = "Name of the product whose stock crossed the threshold")
    private String productName;

    @Schema(description = "Current quantity at the time the alert was recorded")
    private int quantity;

    @Schema(description = "Alert threshold that was crossed")
    private int stockAlertThreshold;

    @Schema(description = "Timestamp when the threshold was crossed")
    private LocalDateTime triggeredAt;

    @Schema(description = "Whether the vendor has acknowledged this alert")
    private boolean acknowledged;

    /**
     * Converts a {@link StockAlert} entity to its API response representation.
     *
     * @param a the stock alert entity
     * @return the response DTO
     */
    public static StockAlertResponse from(StockAlert a) {
        StockAlertResponse r = new StockAlertResponse();
        r.id = a.getId();
        r.productId = a.getProduct().getId();
        r.productName = a.getProduct().getName();
        r.quantity = a.getProduct().getQuantity();
        r.stockAlertThreshold = a.getProduct().getStockAlertThreshold();
        r.triggeredAt = a.getTriggeredAt();
        r.acknowledged = a.isAcknowledged();
        return r;
    }

    /** @return the alert UUID */
    public UUID getId() { return id; }

    /** @return the product UUID */
    public UUID getProductId() { return productId; }

    /** @return the product name */
    public String getProductName() { return productName; }

    /** @return the product quantity at alert time */
    public int getQuantity() { return quantity; }

    /** @return the alert threshold */
    public int getStockAlertThreshold() { return stockAlertThreshold; }

    /** @return the timestamp when the threshold was crossed */
    public LocalDateTime getTriggeredAt() { return triggeredAt; }

    /** @return {@code true} if the vendor acknowledged this alert */
    public boolean isAcknowledged() { return acknowledged; }
}
