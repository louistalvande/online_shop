package com.shop.catalog.entity;

import com.shop.account.entity.Account;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Tracks buyer subscriptions to back-in-stock email alerts (US-SHP-03 / FS-B14).
 * One active subscription per (buyer, product) pair is enforced by a unique constraint.
 */
@Entity
@Table(
    name = "back_in_stock_subscriptions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_back_in_stock_buyer_product",
        columnNames = {"buyer_id", "product_id"}
    )
)
public class BackInStockSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The buyer who requested the alert. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Account buyer;

    /** The product the buyer wants to be notified about. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    /** Timestamp when the subscription was created. */
    private LocalDateTime createdAt;

    /** @return the subscription UUID */
    public UUID getId() { return id; }

    /** @return the subscribing buyer account */
    public Account getBuyer() { return buyer; }

    /** @param buyer the buyer to set */
    public void setBuyer(Account buyer) { this.buyer = buyer; }

    /** @return the product being watched */
    public Product getProduct() { return product; }

    /** @param product the product to set */
    public void setProduct(Product product) { this.product = product; }

    /** @return creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }
}
