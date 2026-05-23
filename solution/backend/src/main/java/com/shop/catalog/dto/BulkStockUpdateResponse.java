package com.shop.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/** Response body for bulk stock update (US-CAT-08). */
public class BulkStockUpdateResponse {

    @Schema(description = "Number of products successfully updated")
    private int totalUpdated;

    @Schema(description = "Number of failed updates")
    private int totalErrors;

    @Schema(description = "Per-product update results")
    private List<StockUpdateResult> results;

    /**
     * Constructs the response with full result data.
     *
     * @param results      per-product results
     * @param totalUpdated count of successful updates
     * @param totalErrors  count of failed updates
     */
    public BulkStockUpdateResponse(List<StockUpdateResult> results, int totalUpdated, int totalErrors) {
        this.results = results;
        this.totalUpdated = totalUpdated;
        this.totalErrors = totalErrors;
    }

    /** @return count of successfully updated products */
    public int getTotalUpdated() { return totalUpdated; }

    /** @return count of failed updates */
    public int getTotalErrors() { return totalErrors; }

    /** @return per-product results */
    public List<StockUpdateResult> getResults() { return results; }

    /** Result for a single product stock update. */
    public static class StockUpdateResult {

        @Schema(description = "Product identifier")
        private UUID productId;

        @Schema(description = "Update status: UPDATED or ERROR")
        private String status;

        @Schema(description = "Error message when status is ERROR, null otherwise")
        private String message;

        @Schema(description = "Updated product data when status is UPDATED, null otherwise")
        private ProductResponse product;

        /**
         * Creates a successful result.
         *
         * @param productId the product UUID
         * @param product   the updated product response
         * @return the success result
         */
        public static StockUpdateResult updated(UUID productId, ProductResponse product) {
            StockUpdateResult r = new StockUpdateResult();
            r.productId = productId;
            r.status = "UPDATED";
            r.product = product;
            return r;
        }

        /**
         * Creates an error result.
         *
         * @param productId the product UUID
         * @param message   the error description
         * @return the error result
         */
        public static StockUpdateResult error(UUID productId, String message) {
            StockUpdateResult r = new StockUpdateResult();
            r.productId = productId;
            r.status = "ERROR";
            r.message = message;
            return r;
        }

        /** @return the product identifier */
        public UUID getProductId() { return productId; }

        /** @return update status (UPDATED or ERROR) */
        public String getStatus() { return status; }

        /** @return error message or null */
        public String getMessage() { return message; }

        /** @return updated product data or null */
        public ProductResponse getProduct() { return product; }
    }
}
