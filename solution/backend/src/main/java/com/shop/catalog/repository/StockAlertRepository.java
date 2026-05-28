package com.shop.catalog.repository;

import com.shop.catalog.entity.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Data access layer for {@link StockAlert} entities. */
public interface StockAlertRepository extends JpaRepository<StockAlert, UUID> {

    /**
     * Returns all alerts matching the given acknowledgment status, ordered by triggered date.
     *
     * @param acknowledged the acknowledgment status to filter on
     * @return list of matching alerts ordered by triggered date descending
     */
    List<StockAlert> findByAcknowledgedOrderByTriggeredAtDesc(boolean acknowledged);

    /**
     * Counts alerts matching the given acknowledgment status.
     *
     * @param acknowledged the acknowledgment status to filter on
     * @return the count of matching alerts
     */
    long countByAcknowledged(boolean acknowledged);

    /**
     * Checks whether any unacknowledged alert already exists for the given product.
     * Used to avoid creating duplicate alerts for the same product (US-CAT-05).
     *
     * @param productId    the product UUID
     * @param acknowledged the acknowledgment status to filter on
     * @return {@code true} if at least one matching alert exists
     */
    boolean existsByProduct_IdAndAcknowledged(UUID productId, boolean acknowledged);
}
