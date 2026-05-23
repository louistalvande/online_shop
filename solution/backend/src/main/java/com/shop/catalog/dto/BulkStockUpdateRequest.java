package com.shop.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** Request body for bulk stock update across multiple products (US-CAT-08). */
public class BulkStockUpdateRequest {

    @Schema(description = "List of per-product stock updates to apply")
    @NotEmpty
    @Valid
    private List<StockUpdateItem> updates;

    /** @return the list of stock update items */
    public List<StockUpdateItem> getUpdates() { return updates; }

    /** @param updates the list of stock update items */
    public void setUpdates(List<StockUpdateItem> updates) { this.updates = updates; }

    /** A single product stock update entry. */
    public static class StockUpdateItem {

        @Schema(description = "Product identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull
        private UUID productId;

        @Schema(description = "New available quantity in stock", example = "15")
        @NotNull
        @Min(0)
        private Integer quantity;

        @Schema(description = "Stock alert threshold — alert triggered when quantity falls below this value", example = "3")
        @Min(0)
        private int stockAlertThreshold = 0;

        /** @return the product identifier */
        public UUID getProductId() { return productId; }

        /** @param productId the product identifier */
        public void setProductId(UUID productId) { this.productId = productId; }

        /** @return the new stock quantity */
        public Integer getQuantity() { return quantity; }

        /** @param quantity the new stock quantity */
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        /** @return the alert threshold */
        public int getStockAlertThreshold() { return stockAlertThreshold; }

        /** @param stockAlertThreshold the alert threshold */
        public void setStockAlertThreshold(int stockAlertThreshold) {
            this.stockAlertThreshold = stockAlertThreshold;
        }
    }
}
