package com.shop.report.service;

import com.shop.report.dto.SalesReportResponse;
import com.shop.report.exception.InvalidPeriodException;

/** Vendor sales report service (US-RPT-01). */
public interface SalesReportService {

    /**
     * Returns aggregated sales metrics and top products for the given vendor and period.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param startDate   start of the reporting period in YYYY-MM-DD format
     * @param endDate     end of the reporting period in YYYY-MM-DD format (inclusive)
     * @param category    optional product category filter; {@code null} means all categories
     * @return the sales report
     * @throws InvalidPeriodException if either date is not in YYYY-MM-DD format
     */
    SalesReportResponse getSalesReport(String vendorEmail, String startDate, String endDate, String category);

    /**
     * Generates a CSV export of the sales report for the given vendor and period.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param startDate   start of the reporting period in YYYY-MM-DD format
     * @param endDate     end of the reporting period in YYYY-MM-DD format (inclusive)
     * @param category    optional product category filter; {@code null} means all categories
     * @return the full CSV content as a UTF-8 string
     * @throws InvalidPeriodException if either date is not in YYYY-MM-DD format
     */
    String exportSalesCsv(String vendorEmail, String startDate, String endDate, String category);
}
