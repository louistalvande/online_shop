package com.shop.seo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request body for creating or replacing a product SEO override. */
public class ProductSeoRequest {

    @Schema(description = "Custom SEO title for the product detail page (overrides product name)")
    private String seoTitle;

    @Schema(description = "Custom meta description for the product detail page")
    private String seoDescription;

    @Schema(description = "Custom meta keywords for the product detail page")
    private String seoKeywords;

    @Schema(description = "Custom Open Graph image URL for the product detail page")
    private String ogImageUrl;

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
}
