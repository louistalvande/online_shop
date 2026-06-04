package com.shop.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Result for a single CSV data row during a product import (US-CAT-06). */
public class CsvImportRowResult {

    @Schema(description = "1-based line number in the CSV file (header counts as line 1)")
    private final int lineNumber;

    @Schema(description = "Row outcome: CREATED, UPDATED or ERROR")
    private final String status;

    @Schema(description = "Error description when status is ERROR; null when status is CREATED")
    private final String message;

    @Schema(description = "Created product when status is CREATED; null when status is ERROR")
    private final ProductResponse product;

    private CsvImportRowResult(int lineNumber, String status, String message, ProductResponse product) {
        this.lineNumber = lineNumber;
        this.status = status;
        this.message = message;
        this.product = product;
    }

    /**
     * Creates a CREATED result for the given line.
     *
     * @param lineNumber 1-based line number
     * @param product    the created product
     * @return a CREATED result
     */
    public static CsvImportRowResult created(int lineNumber, ProductResponse product) {
        return new CsvImportRowResult(lineNumber, "CREATED", null, product);
    }

    /**
     * Creates an UPDATED result for the given line (stock-only merge by id).
     *
     * @param lineNumber 1-based line number
     * @param product    the updated product
     * @return an UPDATED result
     */
    public static CsvImportRowResult updated(int lineNumber, ProductResponse product) {
        return new CsvImportRowResult(lineNumber, "UPDATED", null, product);
    }

    /**
     * Creates an ERROR result for the given line.
     *
     * @param lineNumber 1-based line number
     * @param message    description of the error
     * @return an ERROR result
     */
    public static CsvImportRowResult error(int lineNumber, String message) {
        return new CsvImportRowResult(lineNumber, "ERROR", message, null);
    }

    /** @return the 1-based line number in the CSV file */
    public int getLineNumber() { return lineNumber; }

    /** @return the outcome status: CREATED or ERROR */
    public String getStatus() { return status; }

    /** @return the error description, or {@code null} when status is CREATED */
    public String getMessage() { return message; }

    /** @return the created product, or {@code null} when status is ERROR */
    public ProductResponse getProduct() { return product; }
}
