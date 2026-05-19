package com.shop.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Aggregated sales key performance indicators for a given period (US-RPT-01). */
public record SalesMetrics(

        @Schema(description = "Total revenue for non-cancelled orders, including VAT.")
        BigDecimal revenue,

        @Schema(description = "Number of non-cancelled orders.")
        int orderCount,

        @Schema(description = "Average order value (revenue divided by orderCount).")
        BigDecimal averageCartValue,

        @Schema(description = "Percentage of all orders that were cancelled.")
        BigDecimal cancellationRate
) {}
