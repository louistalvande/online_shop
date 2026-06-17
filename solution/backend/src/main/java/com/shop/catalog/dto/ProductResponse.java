package com.shop.catalog.dto;

import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Read-only representation of a product returned by the API. */
public class ProductResponse {

    @Schema(description = "Product UUID")
    private UUID id;

    @Schema(description = "URL-friendly slug used in public product URLs")
    private String slug;

    @Schema(description = "Product display name")
    private String name;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Pre-tax unit price in euros")
    private BigDecimal priceExclTax;

    @Schema(description = "Product type (poster, carte, affiche…)")
    private String category;

    @Schema(description = "Product theme or occasion (paysage, naissance…)")
    private String theme;

    @Schema(description = "Current available quantity in stock")
    private int quantity;

    @Schema(description = "Stock alert threshold")
    private int stockAlertThreshold;

    @Schema(description = "Lifecycle status: PUBLISHED or ARCHIVED")
    private ProductStatus status;

    @Schema(description = "Ordered list of photo URLs")
    private List<String> photoUrls;

    @Schema(description = "True when quantity is zero")
    private boolean outOfStock;

    @Schema(description = "True when quantity > 0 and quantity <= stockAlertThreshold")
    private boolean belowThreshold;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Converts a {@link Product} entity to its API response representation.
     *
     * @param p the product entity
     * @return the response DTO
     */
    public static ProductResponse from(Product p) {
        ProductResponse r = new ProductResponse();
        r.id = p.getId();
        r.slug = p.getSlug();
        r.name = p.getName();
        r.description = p.getDescription();
        r.priceExclTax = p.getPriceExclTax();
        r.category = p.getCategory();
        r.theme = p.getTheme();
        r.quantity = p.getQuantity();
        r.stockAlertThreshold = p.getStockAlertThreshold();
        r.status = p.getStatus();
        r.photoUrls = p.getPhotos().stream().map(ph -> ph.getUrl()).toList();
        r.outOfStock = p.getQuantity() == 0;
        r.belowThreshold = p.getStockAlertThreshold() > 0
                && p.getQuantity() > 0
                && p.getQuantity() <= p.getStockAlertThreshold();
        r.createdAt = p.getCreatedAt();
        r.updatedAt = p.getUpdatedAt();
        return r;
    }

    /** @return the product UUID */
    public UUID getId() { return id; }

    /** @return the URL-friendly slug */
    public String getSlug() { return slug; }

    /** @return the product display name */
    public String getName() { return name; }

    /** @return the product description */
    public String getDescription() { return description; }

    /** @return the pre-tax price */
    public BigDecimal getPriceExclTax() { return priceExclTax; }

    /** @return the product type */
    public String getCategory() { return category; }

    /** @return the product theme or occasion */
    public String getTheme() { return theme; }

    /** @return the current available quantity */
    public int getQuantity() { return quantity; }

    /** @return the stock alert threshold */
    public int getStockAlertThreshold() { return stockAlertThreshold; }

    /** @return the lifecycle status */
    public ProductStatus getStatus() { return status; }

    /** @return the ordered list of photo URLs */
    public List<String> getPhotoUrls() { return photoUrls; }

    /** @return {@code true} if quantity is zero */
    public boolean isOutOfStock() { return outOfStock; }

    /** @return {@code true} if quantity is above zero but at or below the alert threshold */
    public boolean isBelowThreshold() { return belowThreshold; }

    /** @return the creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @return the last update timestamp */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
