package com.shop.report.service.impl;

import com.shop.report.dto.SalesMetrics;
import com.shop.report.dto.SalesReportResponse;
import com.shop.report.dto.TopProduct;
import com.shop.report.exception.InvalidPeriodException;
import com.shop.report.repository.SalesReportRepository;
import com.shop.report.service.SalesReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
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
    public SalesReportResponse getSalesReport(String vendorEmail, String period, String category) {
        YearMonth ym = parsePeriod(period);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        SalesMetrics metrics = salesReportRepository.computeMetrics(from, to, category);
        List<TopProduct> top = salesReportRepository.findTopProducts(from, to, category, 10);

        return new SalesReportResponse(period, category, metrics, top);
    }

    /** {@inheritDoc} */
    @Override
    public String exportSalesCsv(String vendorEmail, String period, String category) {
        SalesReportResponse report = getSalesReport(vendorEmail, period, category);
        return buildCsv(report);
    }

    private String buildCsv(SalesReportResponse report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sales Report\n");
        sb.append("Period,").append(report.period()).append("\n");
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

    private YearMonth parsePeriod(String period) {
        try {
            return YearMonth.parse(period);
        } catch (DateTimeParseException e) {
            throw new InvalidPeriodException(period);
        }
    }
}
