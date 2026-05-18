package com.shop.catalog.dto;

import com.shop.catalog.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/** Buyer-facing read-only representation of a published product. */
public class BuyerProductResponse {

    private static final BigDecimal VAT_RATE = new BigDecimal("1.20");

    @Schema(description = "Product UUID")
    private UUID id;

    @Schema(description = "Product display name")
    private String name;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Pre-tax unit price in euros")
    private BigDecimal priceExclTax;

    @Schema(description = "All-inclusive price in euros (priceExclTax × 1.20)")
    private BigDecimal priceTTC;

    @Schema(description = "Product category")
    private String category;

    @Schema(description = "Ordered list of photo URLs")
    private List<String> photoUrls;

    @Schema(description = "True when the product is out of stock")
    private boolean outOfStock;

    /**
     * Converts a {@link Product} entity to its buyer-facing response.
     *
     * @param p the product entity
     * @return the buyer response DTO
     */
    public static BuyerProductResponse from(Product p) {
        BuyerProductResponse r = new BuyerProductResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.description = p.getDescription();
        r.priceExclTax = p.getPriceExclTax();
        r.priceTTC = p.getPriceExclTax().multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        r.category = p.getCategory();
        r.photoUrls = p.getPhotos().stream().map(ph -> ph.getUrl()).toList();
        r.outOfStock = p.getQuantity() == 0;
        return r;
    }

    /** @return the product UUID */
    public UUID getId() { return id; }

    /** @return the product display name */
    public String getName() { return name; }

    /** @return the product description */
    public String getDescription() { return description; }

    /** @return the pre-tax unit price */
    public BigDecimal getPriceExclTax() { return priceExclTax; }

    /** @return the all-inclusive price (VAT included) */
    public BigDecimal getPriceTTC() { return priceTTC; }

    /** @return the product category */
    public String getCategory() { return category; }

    /** @return the ordered list of photo URLs */
    public List<String> getPhotoUrls() { return photoUrls; }

    /** @return {@code true} if the product is currently out of stock */
    public boolean isOutOfStock() { return outOfStock; }
}
