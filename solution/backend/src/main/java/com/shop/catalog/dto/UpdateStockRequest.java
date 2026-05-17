package com.shop.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request body for updating stock quantity and alert threshold (US-CAT-04). */
public class UpdateStockRequest {

    @Schema(description = "New available quantity in stock", example = "15")
    @NotNull
    @Min(0)
    private Integer quantity;

    @Schema(description = "Stock alert threshold — alert triggered when quantity falls below this value", example = "3")
    @Min(0)
    private int stockAlertThreshold = 0;

    /** @return the new stock quantity */
    public Integer getQuantity() { return quantity; }

    /** @param quantity the new stock quantity */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    /** @return the alert threshold */
    public int getStockAlertThreshold() { return stockAlertThreshold; }

    /** @param stockAlertThreshold the alert threshold */
    public void setStockAlertThreshold(int stockAlertThreshold) { this.stockAlertThreshold = stockAlertThreshold; }
}
