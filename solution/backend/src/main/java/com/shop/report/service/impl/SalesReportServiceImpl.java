package com.shop.report.service.impl;

import com.shop.report.dto.SalesMetrics;
import com.shop.report.dto.SalesReportResponse;
import com.shop.report.dto.TopProduct;
import com.shop.report.exception.InvalidPeriodException;
import com.shop.report.repository.SalesReportRepository;
import com.shop.report.service.SalesReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Sales report service implementation for the vendor back-office (US-RPT-01). */
@Service
@Transactional(readOnly = true)
public class SalesReportServiceImpl implements SalesReportService {

    private final SalesReportRepository salesReportRepository;

    /**
     * @param salesReportRepository report-specific aggregation repository
     */
    public SalesReportServiceImpl(SalesReportRepository salesReportRepository) {
        this.salesReportRepository = salesReportRepository;
    }

    /** {@inheritDoc} */
    @Override
    public SalesReportResponse getSalesReport(String vendorEmail, String startDate, String endDate, String category) {
        LocalDateTime from = parseDate(startDate).atStartOfDay();
        LocalDateTime to = parseDate(endDate).plusDays(1).atStartOfDay();

        SalesMetrics metrics = salesReportRepository.computeMetrics(from, to, category);
        List<TopProduct> top = salesReportRepository.findTopProducts(from, to, category, 10);

        return new SalesReportResponse(startDate, endDate, category, metrics, top);
    }

    /** {@inheritDoc} */
    @Override
    public String exportSalesCsv(String vendorEmail, String startDate, String endDate, String category) {
        SalesReportResponse report = getSalesReport(vendorEmail, startDate, endDate, category);
        return buildCsv(report);
    }

    private String buildCsv(SalesReportResponse report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sales Report\n");
        sb.append("Start Date,").append(report.startDate()).append("\n");
        sb.append("End Date,").append(report.endDate()).append("\n");
        if (report.category() != null) {
            sb.append("Category,").append(escapeCsv(report.category())).append("\n");
        }
        sb.append("\n");
        sb.append("Key Metrics\n");
        sb.append("Revenue,").append(report.metrics().revenue()).append("\n");
        sb.append("Order Count,").append(report.metrics().orderCount()).append("\n");
        sb.append("Average Cart Value,").append(report.metrics().averageCartValue()).append("\n");
        sb.append("Cancellation Rate (%),").append(report.metrics().cancellationRate()).append("\n");
        sb.append("\n");
        sb.append("Top Selling Products\n");
        sb.append("Rank,Product,Quantity Sold,Revenue Generated\n");
        for (TopProduct p : report.topSellingProducts()) {
            sb.append(p.rank()).append(",")
              .append(escapeCsv(p.productName())).append(",")
              .append(p.quantitySold()).append(",")
              .append(p.revenueGenerated()).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new InvalidPeriodException(date);
        }
    }
}
