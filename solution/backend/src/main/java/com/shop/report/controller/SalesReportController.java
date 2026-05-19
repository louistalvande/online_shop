package com.shop.report.controller;

import com.shop.report.dto.SalesReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

/** Vendor sales report API (US-RPT-01). */
@Tag(name = "Vendor - Reports", description = "Vendor sales reporting endpoints")
@RequestMapping("/api/vendor/reports")
public interface SalesReportController {

    /**
     * Returns aggregated sales metrics and top products for the authenticated vendor.
     *
     * @param principal the authenticated vendor principal
     * @param period    reporting period in YYYY-MM format (required)
     * @param category  optional product category filter
     * @return 200 with the sales report, 400 if period format is invalid
     */
    @Operation(summary = "Get sales report for a period")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales report returned"),
            @ApiResponse(responseCode = "400", description = "Invalid period format"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/sales")
    ResponseEntity<SalesReportResponse> getSalesReport(
            Principal principal,
            @RequestParam String period,
            @RequestParam(required = false) String category);

    /**
     * Exports the sales report as a downloadable CSV file.
     *
     * @param principal the authenticated vendor principal
     * @param period    reporting period in YYYY-MM format (required)
     * @param category  optional product category filter
     * @return 200 with CSV content and attachment headers, 400 if period format is invalid
     */
    @Operation(summary = "Export sales report as a CSV file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file returned"),
            @ApiResponse(responseCode = "400", description = "Invalid period format"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/sales/export")
    ResponseEntity<String> exportSalesCsv(
            Principal principal,
            @RequestParam String period,
            @RequestParam(required = false) String category);
}
