package com.shop.catalog.dto;

import com.shop.catalog.entity.BackInStockSubscription;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Response DTO for a buyer's back-in-stock subscription (US-SHP-03). */
public class StockSubscriptionResponse {

    @Schema(description = "Subscription UUID") private UUID id;
    @Schema(description = "Product UUID") private UUID productId;
    @Schema(description = "Product name") private String productName;
    @Schema(description = "First product photo URL, or null") private String photoUrl;
    @Schema(description = "Product price incl. tax") private BigDecimal priceTTC;
    @Schema(description = "Subscription creation timestamp") private LocalDateTime createdAt;

    /**
     * Builds a response DTO from a subscription entity.
     *
     * @param sub the subscription entity
     * @return the populated DTO
     */
    public static StockSubscriptionResponse from(BackInStockSubscription sub) {
        StockSubscriptionResponse r = new StockSubscriptionResponse();
        r.id          = sub.getId();
        r.productId   = sub.getProduct().getId();
        r.productName = sub.getProduct().getName();
        r.priceTTC    = sub.getProduct().getPriceExclTax(); // TTC = excl. tax (CS-09: no VAT)
        r.photoUrl    = sub.getProduct().getPhotos().isEmpty()
                        ? null
                        : sub.getProduct().getPhotos().get(0).getUrl();
        r.createdAt   = sub.getCreatedAt();
        return r;
    }

    /** @return the subscription UUID */
    public UUID getId() { return id; }

    /** @return the product UUID */
    public UUID getProductId() { return productId; }

    /** @return the product name */
    public String getProductName() { return productName; }

    /** @return the first photo URL, or null */
    public String getPhotoUrl() { return photoUrl; }

    /** @return the product price incl. tax */
    public BigDecimal getPriceTTC() { return priceTTC; }

    /** @return the subscription creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
