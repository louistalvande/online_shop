package com.shop.report.controller.impl;

import com.shop.common.GlobalExceptionHandler;
import com.shop.report.dto.SalesMetrics;
import com.shop.report.dto.SalesReportResponse;
import com.shop.report.exception.InvalidPeriodException;
import com.shop.report.service.SalesReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Unit tests for {@link SalesReportControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class SalesReportControllerImplTest {

    @Mock SalesReportService salesReportService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final String VENDOR_EMAIL = "vendor@test.com";

    private final UsernamePasswordAuthenticationToken vendorPrincipal =
            new UsernamePasswordAuthenticationToken(VENDOR_EMAIL, null, List.of());

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new SalesReportControllerImpl(salesReportService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    @Test
    void getSalesReport_returns200WithBody() throws Exception {
        SalesMetrics metrics = new SalesMetrics(
                new BigDecimal("100.00"), 3, new BigDecimal("33.33"), BigDecimal.ZERO);
        SalesReportResponse report = new SalesReportResponse("2025-01", null, metrics, List.of());

        given(salesReportService.getSalesReport(eq(VENDOR_EMAIL), eq("2025-01"), isNull()))
                .willReturn(report);

        mvc.perform(get("/api/vendor/reports/sales")
                        .principal(vendorPrincipal)
                        .param("period", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("2025-01"))
                .andExpect(jsonPath("$.metrics.orderCount").value(3))
                .andExpect(jsonPath("$.metrics.revenue").value(100.00))
                .andExpect(jsonPath("$.topSellingProducts").isArray());
    }

    @Test
    void getSalesReport_withCategory_passesParamToService() throws Exception {
        SalesMetrics metrics = new SalesMetrics(
                new BigDecimal("50.00"), 1, new BigDecimal("50.00"), BigDecimal.ZERO);
        SalesReportResponse report = new SalesReportResponse("2025-01", "PAINTING", metrics, List.of());

        given(salesReportService.getSalesReport(eq(VENDOR_EMAIL), eq("2025-01"), eq("PAINTING")))
                .willReturn(report);

        mvc.perform(get("/api/vendor/reports/sales")
                        .principal(vendorPrincipal)
                        .param("period", "2025-01")
                        .param("category", "PAINTING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("PAINTING"));
    }

    @Test
    void getSalesReport_invalidPeriod_returns400() throws Exception {
        given(salesReportService.getSalesReport(eq(VENDOR_EMAIL), eq("bad"), isNull()))
                .willThrow(new InvalidPeriodException("bad"));
        given(messageSource.getMessage(eq("error.report.invalid.period"), any(), any(Locale.class)))
                .willReturn("Invalid period format");

        mvc.perform(get("/api/vendor/reports/sales")
                        .principal(vendorPrincipal)
                        .param("period", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PERIOD"));
    }

    @Test
    void exportSalesCsv_returns200WithCsvContentType() throws Exception {
        given(salesReportService.exportSalesCsv(eq(VENDOR_EMAIL), eq("2025-01"), isNull()))
                .willReturn("Sales Report\nPeriod,2025-01\n\nKey Metrics\nRevenue,100.00\n");

        mvc.perform(get("/api/vendor/reports/sales/export")
                        .principal(vendorPrincipal)
                        .param("period", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("sales-report-2025-01.csv")))
                .andExpect(content().string(containsString("Sales Report")));
    }
}
