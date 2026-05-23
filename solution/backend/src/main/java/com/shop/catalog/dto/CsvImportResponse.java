package com.shop.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Summary of a CSV product import operation (US-CAT-06). */
public class CsvImportResponse {

    @Schema(description = "Per-row results in file order")
    private final List<CsvImportRowResult> rows;

    @Schema(description = "Total number of successfully created products")
    private final int totalCreated;

    @Schema(description = "Total number of rows that failed to import")
    private final int totalErrors;

    /**
     * Constructs the import response.
     *
     * @param rows         per-row import results
     * @param totalCreated number of successfully imported rows
     * @param totalErrors  number of failed rows
     */
    public CsvImportResponse(List<CsvImportRowResult> rows, int totalCreated, int totalErrors) {
        this.rows = rows;
        this.totalCreated = totalCreated;
        this.totalErrors = totalErrors;
    }

    /** @return per-row import results in file order */
    public List<CsvImportRowResult> getRows() { return rows; }

    /** @return number of successfully created products */
    public int getTotalCreated() { return totalCreated; }

    /** @return number of rows that failed to import */
    public int getTotalErrors() { return totalErrors; }
}
