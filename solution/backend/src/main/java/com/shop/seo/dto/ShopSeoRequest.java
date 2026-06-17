package com.shop.seo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request body for creating or updating the shop-wide SEO configuration. */
public class ShopSeoRequest {

    @Schema(description = "HTML title tag for the shop home page")
    private String seoTitle;

    @Schema(description = "Meta description for the shop home page")
    private String seoDescription;

    @Schema(description = "Comma-separated meta keywords for the shop home page")
    private String seoKeywords;

    @Schema(description = "Open Graph image URL used when sharing on social networks")
    private String ogImageUrl;

    @Schema(description = "Canonical URL for the shop home page")
    private String canonicalUrl;

    @Schema(description = "Newline-separated paths to disallow in robots.txt")
    private String robotsDisallowPaths;

    @Schema(description = "Sitemap change-frequency hint (daily, weekly, monthly…)", example = "weekly")
    private String sitemapChangefreq;

    @Schema(description = "Whether product detail pages should be indexed by search engines")
    private Boolean indexProducts;

    @Schema(description = "Whether the catalog listing page should be indexed by search engines")
    private Boolean indexCatalog;

    @Schema(description = "Whether account pages should be indexed by search engines")
    private Boolean indexAccount;

    @Schema(description = "Whether the cart page should be indexed by search engines")
    private Boolean indexCart;

    @Schema(description = "Google Search Console site verification token")
    private String googleVerification;

    @Schema(description = "Google Analytics 4 measurement ID (G-XXXXXXXXXX)")
    private String ga4Id;

    @Schema(description = "Bing Webmaster Tools site verification token")
    private String bingVerification;

    /** Returns the SEO title. */
    public String getSeoTitle() { return seoTitle; }

    /** Sets the SEO title. @param seoTitle the title */
    public void setSeoTitle(String seoTitle) { this.seoTitle = seoTitle; }

    /** Returns the SEO description. */
    public String getSeoDescription() { return seoDescription; }

    /** Sets the SEO description. @param seoDescription the description */
    public void setSeoDescription(String seoDescription) { this.seoDescription = seoDescription; }

    /** Returns the SEO keywords. */
    public String getSeoKeywords() { return seoKeywords; }

    /** Sets the SEO keywords. @param seoKeywords comma-separated keywords */
    public void setSeoKeywords(String seoKeywords) { this.seoKeywords = seoKeywords; }

    /** Returns the Open Graph image URL. */
    public String getOgImageUrl() { return ogImageUrl; }

    /** Sets the Open Graph image URL. @param ogImageUrl the URL */
    public void setOgImageUrl(String ogImageUrl) { this.ogImageUrl = ogImageUrl; }

    /** Returns the canonical URL. */
    public String getCanonicalUrl() { return canonicalUrl; }

    /** Sets the canonical URL. @param canonicalUrl the URL */
    public void setCanonicalUrl(String canonicalUrl) { this.canonicalUrl = canonicalUrl; }

    /** Returns the robots.txt disallow paths. */
    public String getRobotsDisallowPaths() { return robotsDisallowPaths; }

    /** Sets the robots.txt disallow paths. @param robotsDisallowPaths newline-separated paths */
    public void setRobotsDisallowPaths(String robotsDisallowPaths) { this.robotsDisallowPaths = robotsDisallowPaths; }

    /** Returns the sitemap change frequency. */
    public String getSitemapChangefreq() { return sitemapChangefreq; }

    /** Sets the sitemap change frequency. @param sitemapChangefreq the frequency */
    public void setSitemapChangefreq(String sitemapChangefreq) { this.sitemapChangefreq = sitemapChangefreq; }

    /** Returns whether product pages are indexed. */
    public Boolean getIndexProducts() { return indexProducts; }

    /** Sets whether product pages are indexed. @param indexProducts the flag */
    public void setIndexProducts(Boolean indexProducts) { this.indexProducts = indexProducts; }

    /** Returns whether the catalog page is indexed. */
    public Boolean getIndexCatalog() { return indexCatalog; }

    /** Sets whether the catalog page is indexed. @param indexCatalog the flag */
    public void setIndexCatalog(Boolean indexCatalog) { this.indexCatalog = indexCatalog; }

    /** Returns whether account pages are indexed. */
    public Boolean getIndexAccount() { return indexAccount; }

    /** Sets whether account pages are indexed. @param indexAccount the flag */
    public void setIndexAccount(Boolean indexAccount) { this.indexAccount = indexAccount; }

    /** Returns whether the cart page is indexed. */
    public Boolean getIndexCart() { return indexCart; }

    /** Sets whether the cart page is indexed. @param indexCart the flag */
    public void setIndexCart(Boolean indexCart) { this.indexCart = indexCart; }

    /** Returns the Google verification token. */
    public String getGoogleVerification() { return googleVerification; }

    /** Sets the Google verification token. @param googleVerification the token */
    public void setGoogleVerification(String googleVerification) { this.googleVerification = googleVerification; }

    /** Returns the GA4 measurement ID. */
    public String getGa4Id() { return ga4Id; }

    /** Sets the GA4 measurement ID. @param ga4Id the measurement ID */
    public void setGa4Id(String ga4Id) { this.ga4Id = ga4Id; }

    /** Returns the Bing verification token. */
    public String getBingVerification() { return bingVerification; }

    /** Sets the Bing verification token. @param bingVerification the token */
    public void setBingVerification(String bingVerification) { this.bingVerification = bingVerification; }
}
