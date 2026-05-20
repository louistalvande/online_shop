package com.shop.report.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.repository.AccountRepository;
import com.shop.report.dto.SalesMetrics;
import com.shop.report.dto.SalesReportResponse;
import com.shop.report.dto.TopProduct;
import com.shop.report.exception.InvalidPeriodException;
import com.shop.report.repository.SalesReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/** Unit tests for {@link SalesReportServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class SalesReportServiceImplTest {

    @Mock AccountRepository accountRepository;
    @Mock SalesReportRepository salesReportRepository;

    SalesReportServiceImpl service;

    private static final String VENDOR_EMAIL = "vendor@test.com";
    private static final UUID VENDOR_ID = UUID.randomUUID();
    private static final String PERIOD = "2025-01";

    private static final SalesMetrics ZERO_METRICS =
            new SalesMetrics(BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO);

    @BeforeEach
    void setUp() {
        service = new SalesReportServiceImpl(accountRepository, salesReportRepository);
        Account account = mock(Account.class);
        given(account.getId()).willReturn(VENDOR_ID);
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(account));
    }

    @Test
    void getSalesReport_nominal_returnsReport() {
        SalesMetrics metrics = new SalesMetrics(
                new BigDecimal("100.00"), 5, new BigDecimal("20.00"), BigDecimal.ZERO);
        List<TopProduct> products = List.of(
                new TopProduct(1, "Product A", 3, new BigDecimal("60.00")));

        given(salesReportRepository.computeMetrics(eq(VENDOR_ID), any(LocalDateTime.class),
                any(LocalDateTime.class), isNull())).willReturn(metrics);
        given(salesReportRepository.findTopProducts(eq(VENDOR_ID), any(LocalDateTime.class),
                any(LocalDateTime.class), isNull(), eq(10))).willReturn(products);

        SalesReportResponse result = service.getSalesReport(VENDOR_EMAIL, PERIOD, null);

        assertThat(result.period()).isEqualTo(PERIOD);
        assertThat(result.category()).isNull();
        assertThat(result.metrics().orderCount()).isEqualTo(5);
        assertThat(result.metrics().revenue()).isEqualByComparingTo("100.00");
        assertThat(result.topSellingProducts()).hasSize(1);
        assertThat(result.topSellingProducts().get(0).rank()).isEqualTo(1);
    }

    @Test
    void getSalesReport_withCategory_passesCategory() {
        SalesMetrics metrics = new SalesMetrics(
                new BigDecimal("50.00"), 2, new BigDecimal("25.00"), BigDecimal.ZERO);

        given(salesReportRepository.computeMetrics(eq(VENDOR_ID), any(LocalDateTime.class),
                any(LocalDateTime.class), eq("PAINTING"))).willReturn(metrics);
        given(salesReportRepository.findTopProducts(eq(VENDOR_ID), any(LocalDateTime.class),
                any(LocalDateTime.class), eq("PAINTING"), eq(10))).willReturn(List.of());

        SalesReportResponse result = service.getSalesReport(VENDOR_EMAIL, PERIOD, "PAINTING");

        assertThat(result.category()).isEqualTo("PAINTING");
        assertThat(result.metrics().orderCount()).isEqualTo(2);
    }

    @Test
    void getSalesReport_noOrders_returnsZeroMetrics() {
        given(salesReportRepository.computeMetrics(any(), any(), any(), isNull()))
                .willReturn(ZERO_METRICS);
        given(salesReportRepository.findTopProducts(any(), any(), any(), isNull(), anyInt()))
                .willReturn(List.of());

        SalesReportResponse result = service.getSalesReport(VENDOR_EMAIL, PERIOD, null);

        assertThat(result.metrics().orderCount()).isZero();
        assertThat(result.metrics().revenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.topSellingProducts()).isEmpty();
    }

    @Test
    void getSalesReport_invalidPeriod_throwsInvalidPeriodException() {
        assertThatThrownBy(() -> service.getSalesReport(VENDOR_EMAIL, "invalid", null))
                .isInstanceOf(InvalidPeriodException.class);
    }

    @Test
    void getSalesReport_partialPeriod_throwsInvalidPeriodException() {
        assertThatThrownBy(() -> service.getSalesReport(VENDOR_EMAIL, "2025", null))
                .isInstanceOf(InvalidPeriodException.class);
    }

    @Test
    void exportSalesCsv_nominal_containsExpectedSections() {
        SalesMetrics metrics = new SalesMetrics(
                new BigDecimal("200.00"), 4, new BigDecimal("50.00"), new BigDecimal("10.00"));
        List<TopProduct> products = List.of(
                new TopProduct(1, "Widget,A", 3, new BigDecimal("90.00")),
                new TopProduct(2, "Normal Product", 1, new BigDecimal("110.00")));

        given(salesReportRepository.computeMetrics(any(), any(), any(), isNull()))
                .willReturn(metrics);
        given(salesReportRepository.findTopProducts(any(), any(), any(), isNull(), anyInt()))
                .willReturn(products);

        String csv = service.exportSalesCsv(VENDOR_EMAIL, PERIOD, null);

        assertThat(csv).contains("Sales Report");
        assertThat(csv).contains("Period," + PERIOD);
        assertThat(csv).contains("Revenue,200.00");
        assertThat(csv).contains("Order Count,4");
        assertThat(csv).contains("Cancellation Rate (%),10.00");
        assertThat(csv).contains("Top Selling Products");
        assertThat(csv).contains("\"Widget,A\""); // CSV escaping for value containing comma
        assertThat(csv).contains("Normal Product");
    }

    @Test
    void exportSalesCsv_withCategory_includesCategoryLine() {
        SalesMetrics metrics = new SalesMetrics(
                new BigDecimal("30.00"), 1, new BigDecimal("30.00"), BigDecimal.ZERO);

        given(salesReportRepository.computeMetrics(any(), any(), any(), eq("JEWELRY")))
                .willReturn(metrics);
        given(salesReportRepository.findTopProducts(any(), any(), any(), eq("JEWELRY"), anyInt()))
                .willReturn(List.of());

        String csv = service.exportSalesCsv(VENDOR_EMAIL, PERIOD, "JEWELRY");

        assertThat(csv).contains("Category,JEWELRY");
    }
}
