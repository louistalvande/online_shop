package com.shop.report.repository;

import com.shop.report.dto.SalesMetrics;
import com.shop.report.dto.TopProduct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Native SQL aggregation queries for vendor sales reports (US-RPT-01). */
@Repository
public class SalesReportRepository {

    @PersistenceContext
    private EntityManager em;

    private static final String METRICS_SQL = """
            SELECT COUNT(*),
                   COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END),
                   COALESCE(SUM(CASE WHEN status != 'CANCELLED' THEN total_amount_ttc ELSE 0 END), 0)
            FROM orders
            WHERE vendor_id = :vendorId
              AND created_at >= :from
              AND created_at < :to
            """;

    private static final String METRICS_SQL_CATEGORY = """
            WITH matched AS (
              SELECT DISTINCT o.id
              FROM orders o
              JOIN order_lines ol ON ol.order_id = o.id
              JOIN products p ON p.id = ol.product_id
              WHERE o.vendor_id = :vendorId
                AND o.created_at >= :from
                AND o.created_at < :to
                AND p.category = :category
            )
            SELECT COUNT(*),
                   COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END),
                   COALESCE(SUM(CASE WHEN status != 'CANCELLED' THEN total_amount_ttc ELSE 0 END), 0)
            FROM orders
            WHERE id IN (SELECT id FROM matched)
            """;

    private static final String TOP_PRODUCTS_SQL = """
            SELECT ol.product_name,
                   SUM(ol.quantity),
                   SUM(ol.line_total_ttc)
            FROM order_lines ol
            JOIN orders o ON o.id = ol.order_id
            WHERE o.vendor_id = :vendorId
              AND o.status != 'CANCELLED'
              AND o.created_at >= :from
              AND o.created_at < :to
            GROUP BY ol.product_name
            ORDER BY SUM(ol.quantity) DESC, SUM(ol.line_total_ttc) DESC
            """;

    private static final String TOP_PRODUCTS_SQL_CATEGORY = """
            SELECT ol.product_name,
                   SUM(ol.quantity),
                   SUM(ol.line_total_ttc)
            FROM order_lines ol
            JOIN orders o ON o.id = ol.order_id
            JOIN products p ON p.id = ol.product_id
            WHERE o.vendor_id = :vendorId
              AND o.status != 'CANCELLED'
              AND o.created_at >= :from
              AND o.created_at < :to
              AND p.category = :category
            GROUP BY ol.product_name
            ORDER BY SUM(ol.quantity) DESC, SUM(ol.line_total_ttc) DESC
            """;

    /**
     * Computes aggregated sales metrics for the given vendor and date range.
     *
     * @param vendorId the vendor account UUID
     * @param from     inclusive start of the period (start of the first day)
     * @param to       exclusive end of the period (start of the first day of the next month)
     * @param category optional product category filter; {@code null} means all categories
     * @return the computed metrics
     */
    public SalesMetrics computeMetrics(UUID vendorId, LocalDateTime from, LocalDateTime to, String category) {
        String sql = category != null ? METRICS_SQL_CATEGORY : METRICS_SQL;
        Query q = em.createNativeQuery(sql);
        q.setParameter("vendorId", vendorId);
        q.setParameter("from", from);
        q.setParameter("to", to);
        if (category != null) {
            q.setParameter("category", category);
        }

        Object[] row = (Object[]) q.getSingleResult();
        long totalOrders = ((Number) row[0]).longValue();
        long cancelledCount = row[1] == null ? 0L : ((Number) row[1]).longValue();
        BigDecimal revenue = (BigDecimal) row[2];

        long activeOrders = totalOrders - cancelledCount;
        BigDecimal averageCartValue = activeOrders > 0
                ? revenue.divide(BigDecimal.valueOf(activeOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal cancellationRate = totalOrders > 0
                ? BigDecimal.valueOf((double) cancelledCount / totalOrders * 100.0)
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new SalesMetrics(
                revenue.setScale(2, RoundingMode.HALF_UP),
                (int) activeOrders,
                averageCartValue,
                cancellationRate);
    }

    /**
     * Returns the top-selling products for the given vendor and date range.
     *
     * @param vendorId the vendor account UUID
     * @param from     inclusive start of the period
     * @param to       exclusive end of the period
     * @param category optional product category filter; {@code null} means all categories
     * @param limit    maximum number of top products to return
     * @return list of top products ranked by quantity sold descending
     */
    @SuppressWarnings("unchecked")
    public List<TopProduct> findTopProducts(UUID vendorId, LocalDateTime from, LocalDateTime to,
                                            String category, int limit) {
        String sql = category != null ? TOP_PRODUCTS_SQL_CATEGORY : TOP_PRODUCTS_SQL;
        Query q = em.createNativeQuery(sql);
        q.setParameter("vendorId", vendorId);
        q.setParameter("from", from);
        q.setParameter("to", to);
        if (category != null) {
            q.setParameter("category", category);
        }
        q.setMaxResults(limit);

        List<Object[]> rows = q.getResultList();
        List<TopProduct> result = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            Object[] r = rows.get(i);
            String productName = (String) r[0];
            int quantitySold = ((Number) r[1]).intValue();
            BigDecimal revenueGenerated = ((BigDecimal) r[2]).setScale(2, RoundingMode.HALF_UP);
            result.add(new TopProduct(i + 1, productName, quantitySold, revenueGenerated));
        }
        return result;
    }
}
