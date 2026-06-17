package com.shop.seo.dto;

import com.shop.seo.entity.ProductSeoOverride;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/** Read-only view of a per-product SEO override. */
public class ProductSeoResponse {

    @Schema(description = "Product UUID")
    private UUID productId;

    @Schema(description = "Custom SEO title (null means fall back to product name)")
    private String seoTitle;

    @Schema(description = "Custom meta description")
    private String seoDescription;

    @Schema(description = "Custom meta keywords")
    private String seoKeywords;

    @Schema(description = "Custom Open Graph image URL")
    private String ogImageUrl;

    /**
     * Builds a response DTO from a {@link ProductSeoOverride} entity.
     *
     * @param entity the entity to map
     * @return the populated response DTO
     */
    public static ProductSeoResponse from(ProductSeoOverride entity) {
        ProductSeoResponse r = new ProductSeoResponse();
        r.productId = entity.getProductId();
        r.seoTitle = entity.getSeoTitle();
        r.seoDescription = entity.getSeoDescription();
        r.seoKeywords = entity.getSeoKeywords();
        r.ogImageUrl = entity.getOgImageUrl();
        return r;
    }

    /** Returns the product UUID. */
    public UUID getProductId() { return productId; }

    /** Returns the custom SEO title. */
    public String getSeoTitle() { return seoTitle; }

    /** Returns the custom meta description. */
    public String getSeoDescription() { return seoDescription; }

    /** Returns the custom meta keywords. */
    public String getSeoKeywords() { return seoKeywords; }

    /** Returns the custom Open Graph image URL. */
    public String getOgImageUrl() { return ogImageUrl; }
}
