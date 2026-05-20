package com.shop.report.service;

import com.shop.report.dto.SalesReportResponse;
import com.shop.report.exception.InvalidPeriodException;

/** Vendor sales report service (US-RPT-01). */
public interface SalesReportService {

    /**
     * Returns aggregated sales metrics and top products for the given vendor and period.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param period      reporting period in YYYY-MM format
     * @param category    optional product category filter; {@code null} means all categories
     * @return the sales report
     * @throws InvalidPeriodException if {@code period} is not in YYYY-MM format
     */
    SalesReportResponse getSalesReport(String vendorEmail, String period, String category);

    /**
     * Generates a CSV export of the sales report for the given vendor and period.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param period      reporting period in YYYY-MM format
     * @param category    optional product category filter; {@code null} means all categories
     * @return the full CSV content as a UTF-8 string
     * @throws InvalidPeriodException if {@code period} is not in YYYY-MM format
     */
    String exportSalesCsv(String vendorEmail, String period, String category);
}
