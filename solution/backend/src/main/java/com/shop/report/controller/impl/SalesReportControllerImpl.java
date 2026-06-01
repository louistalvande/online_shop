package com.shop.report.controller.impl;

import com.shop.report.controller.SalesReportController;
import com.shop.report.dto.SalesReportResponse;
import com.shop.report.service.SalesReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** Implementation of {@link SalesReportController}. */
@RestController
public class SalesReportControllerImpl implements SalesReportController {

    private final SalesReportService salesReportService;

    /**
     * @param salesReportService the sales report service
     */
    public SalesReportControllerImpl(SalesReportService salesReportService) {
        this.salesReportService = salesReportService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<SalesReportResponse> getSalesReport(
            Principal principal, String startDate, String endDate, String category) {
        SalesReportResponse report = salesReportService.getSalesReport(
                principal.getName(), startDate, endDate, category);
        return ResponseEntity.ok(report);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<String> exportSalesCsv(
            Principal principal, String startDate, String endDate, String category) {
        String csv = salesReportService.exportSalesCsv(principal.getName(), startDate, endDate, category);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("sales-report-" + startDate + "-" + endDate + ".csv")
                        .build());
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
