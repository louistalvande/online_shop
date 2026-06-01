package com.shop.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Full vendor sales report for a given period (US-RPT-01). */
public record SalesReportResponse(

        @Schema(description = "Start of the reporting period in YYYY-MM-DD format.")
        String startDate,

        @Schema(description = "End of the reporting period in YYYY-MM-DD format (inclusive).")
        String endDate,

        @Schema(description = "Optional product category filter applied to this report; null means all categories.")
        String category,

        @Schema(description = "Aggregated key performance indicators for the period.")
        SalesMetrics metrics,

        @Schema(description = "Up to 10 top-selling products ranked by quantity sold.")
        List<TopProduct> topSellingProducts
) {}
