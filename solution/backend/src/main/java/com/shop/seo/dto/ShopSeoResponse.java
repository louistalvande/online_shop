package com.shop.seo.dto;

import com.shop.seo.entity.ShopSeo;
import io.swagger.v3.oas.annotations.media.Schema;

/** Read-only view of the shop-wide SEO configuration. */
public class ShopSeoResponse {

    @Schema(description = "HTML title tag for the shop home page")
    private String seoTitle;

    @Schema(description = "Meta description for the shop home page")
    private String seoDescription;

    @Schema(description = "Comma-separated meta keywords for the shop home page")
    private String seoKeywords;

    @Schema(description = "Open Graph image URL")
    private String ogImageUrl;

    @Schema(description = "Canonical URL for the shop home page")
    private String canonicalUrl;

    @Schema(description = "Newline-separated paths to disallow in robots.txt")
    private String robotsDisallowPaths;

    @Schema(description = "Sitemap change-frequency hint", example = "weekly")
    private String sitemapChangefreq;

    @Schema(description = "Whether product pages are indexed")
    private boolean indexProducts;

    @Schema(description = "Whether the catalog page is indexed")
    private boolean indexCatalog;

    @Schema(description = "Google Search Console site verification token")
    private String googleVerification;

    @Schema(description = "Google Analytics 4 measurement ID")
    private String ga4Id;

    @Schema(description = "Bing Webmaster Tools site verification token")
    private String bingVerification;

    /**
     * Builds a response DTO from a {@link ShopSeo} entity.
     *
     * @param entity the entity to map
     * @return the populated response DTO
     */
    public static ShopSeoResponse from(ShopSeo entity) {
        ShopSeoResponse r = new ShopSeoResponse();
        r.seoTitle = entity.getSeoTitle();
        r.seoDescription = entity.getSeoDescription();
        r.seoKeywords = entity.getSeoKeywords();
        r.ogImageUrl = entity.getOgImageUrl();
        r.canonicalUrl = entity.getCanonicalUrl();
        r.robotsDisallowPaths = entity.getRobotsDisallowPaths();
        r.sitemapChangefreq = entity.getSitemapChangefreq();
        r.indexProducts = entity.isIndexProducts();
        r.indexCatalog = entity.isIndexCatalog();
        r.googleVerification = entity.getGoogleVerification();
        r.ga4Id = entity.getGa4Id();
        r.bingVerification = entity.getBingVerification();
        return r;
    }

    /** Returns the SEO title. */
    public String getSeoTitle() { return seoTitle; }

    /** Returns the SEO description. */
    public String getSeoDescription() { return seoDescription; }

    /** Returns the SEO keywords. */
    public String getSeoKeywords() { return seoKeywords; }

    /** Returns the Open Graph image URL. */
    public String getOgImageUrl() { return ogImageUrl; }

    /** Returns the canonical URL. */
    public String getCanonicalUrl() { return canonicalUrl; }

    /** Returns the robots.txt disallow paths. */
    public String getRobotsDisallowPaths() { return robotsDisallowPaths; }

    /** Returns the sitemap change frequency. */
    public String getSitemapChangefreq() { return sitemapChangefreq; }

    /** Returns whether product pages are indexed. */
    public boolean isIndexProducts() { return indexProducts; }

    /** Returns whether the catalog page is indexed. */
    public boolean isIndexCatalog() { return indexCatalog; }

    /** Returns the Google verification token. */
    public String getGoogleVerification() { return googleVerification; }

    /** Returns the GA4 measurement ID. */
    public String getGa4Id() { return ga4Id; }

    /** Returns the Bing verification token. */
    public String getBingVerification() { return bingVerification; }
}
