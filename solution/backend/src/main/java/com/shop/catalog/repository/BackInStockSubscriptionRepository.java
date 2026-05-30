package com.shop.catalog.repository;

import com.shop.catalog.entity.BackInStockSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository for back-in-stock buyer alert subscriptions (US-SHP-03). */
public interface BackInStockSubscriptionRepository extends JpaRepository<BackInStockSubscription, UUID> {

    /**
     * Returns all pending subscriptions for a given product.
     * Used to fan out notification emails when the product is restocked.
     * Subscriptions are deleted after notification, so all records here are pending.
     *
     * @param productId the product UUID
     * @return list of subscriptions to notify
     */
    List<BackInStockSubscription> findByProduct_Id(UUID productId);

    /**
     * Returns all subscriptions for a given buyer, newest first.
     *
     * @param buyerId the buyer account UUID
     * @return list of the buyer's active subscriptions
     */
    List<BackInStockSubscription> findByBuyer_IdOrderByCreatedAtDesc(UUID buyerId);

    /**
     * Finds the subscription for a (buyer, product) pair.
     *
     * @param buyerId   the buyer account UUID
     * @param productId the product UUID
     * @return the subscription if it exists
     */
    Optional<BackInStockSubscription> findByBuyer_IdAndProduct_Id(UUID buyerId, UUID productId);

    /**
     * Checks whether a subscription exists for a (buyer, product) pair.
     *
     * @param buyerId   the buyer account UUID
     * @param productId the product UUID
     * @return true if a subscription exists
     */
    boolean existsByBuyer_IdAndProduct_Id(UUID buyerId, UUID productId);
}
