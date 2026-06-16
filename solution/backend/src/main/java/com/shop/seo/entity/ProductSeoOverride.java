package com.shop.seo.entity;

import com.shop.catalog.entity.Product;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** Per-product SEO override settings (US-SEO-02). All fields are optional; null means "fall back to product data". */
@Entity
@Table(name = "product_seo_override")
public class ProductSeoOverride {

    /** Product UUID — also the primary key (one-to-one mapping). */
    @Id
    private UUID productId;

    /** Product this override belongs to. */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    /** Custom SEO title for the product detail page (overrides the product name). */
    @Column(length = 255)
    private String seoTitle;

    /** Custom meta description for the product detail page. */
    @Column(length = 500)
    private String seoDescription;

    /** Custom meta keywords for the product detail page. */
    @Column(length = 500)
    private String seoKeywords;

    /** Custom Open Graph image URL for the product detail page. */
    @Column(name = "og_image_url", length = 500)
    private String ogImageUrl;

    /** Timestamp when this override was first created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Timestamp of the last update to this override. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** Returns the product UUID / primary key. */
    public UUID getProductId() { return productId; }

    /** Sets the product UUID. @param productId the UUID */
    public void setProductId(UUID productId) { this.productId = productId; }

    /** Returns the associated product. */
    public Product getProduct() { return product; }

    /** Sets the associated product. @param product the product entity */
    public void setProduct(Product product) { this.product = product; }

    /** Returns the custom SEO title. */
    public String getSeoTitle() { return seoTitle; }

    /** Sets the custom SEO title. @param seoTitle the title */
    public void setSeoTitle(String seoTitle) { this.seoTitle = seoTitle; }

    /** Returns the custom meta description. */
    public String getSeoDescription() { return seoDescription; }

    /** Sets the custom meta description. @param seoDescription the description */
    public void setSeoDescription(String seoDescription) { this.seoDescription = seoDescription; }

    /** Returns the custom meta keywords. */
    public String getSeoKeywords() { return seoKeywords; }

    /** Sets the custom meta keywords. @param seoKeywords comma-separated keywords */
    public void setSeoKeywords(String seoKeywords) { this.seoKeywords = seoKeywords; }

    /** Returns the custom Open Graph image URL. */
    public String getOgImageUrl() { return ogImageUrl; }

    /** Sets the custom Open Graph image URL. @param ogImageUrl the URL */
    public void setOgImageUrl(String ogImageUrl) { this.ogImageUrl = ogImageUrl; }

    /** Returns the creation timestamp. */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** Sets the creation timestamp. @param createdAt the timestamp */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Returns the last update timestamp. */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /** Sets the last update timestamp. @param updatedAt the timestamp */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
