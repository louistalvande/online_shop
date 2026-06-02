package com.shop.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Summary of a CSV product import operation (US-CAT-06). */
public class CsvImportResponse {

    @Schema(description = "Per-row results in file order")
    private final List<CsvImportRowResult> rows;

    @Schema(description = "Total number of successfully created products")
    private final int totalCreated;

    @Schema(description = "Total number of products whose stock was updated by id")
    private final int totalUpdated;

    @Schema(description = "Total number of rows that failed to import")
    private final int totalErrors;

    /**
     * Constructs the import response.
     *
     * @param rows         per-row import results
     * @param totalCreated number of successfully created rows
     * @param totalUpdated number of successfully stock-updated rows
     * @param totalErrors  number of failed rows
     */
    public CsvImportResponse(List<CsvImportRowResult> rows, int totalCreated, int totalUpdated, int totalErrors) {
        this.rows = rows;
        this.totalCreated = totalCreated;
        this.totalUpdated = totalUpdated;
        this.totalErrors = totalErrors;
    }

    /** @return per-row import results in file order */
    public List<CsvImportRowResult> getRows() { return rows; }

    /** @return number of successfully created products */
    public int getTotalCreated() { return totalCreated; }

    /** @return number of products whose stock was updated by id */
    public int getTotalUpdated() { return totalUpdated; }

    /** @return number of rows that failed to import */
    public int getTotalErrors() { return totalErrors; }
}
