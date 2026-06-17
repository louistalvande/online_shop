package com.shop.seo.service;

import com.shop.seo.dto.ProductSeoRequest;
import com.shop.seo.dto.ProductSeoResponse;
import com.shop.seo.dto.ShopSeoRequest;
import com.shop.seo.dto.ShopSeoResponse;
import com.shop.seo.exception.SeoNotFoundException;

import java.util.UUID;

/** Business operations for shop and product SEO configuration (US-SEO-01 to US-SEO-05). */
public interface SeoService {

    /**
     * Returns the current shop-wide SEO configuration for the authenticated vendor.
     * Returns an empty configuration (all nulls / defaults) if no record exists yet.
     *
     * @return the shop SEO configuration
     */
    ShopSeoResponse getShopSeo();

    /**
     * Creates or replaces the shop-wide SEO configuration.
     *
     * @param request the new configuration values
     * @return the persisted configuration
     */
    ShopSeoResponse saveShopSeo(ShopSeoRequest request);

    /**
     * Creates or replaces the SEO override for a specific product.
     *
     * @param productId the product UUID
     * @param request   the override values
     * @return the persisted override
     */
    ProductSeoResponse saveProductSeo(UUID productId, ProductSeoRequest request);

    /**
     * Returns the SEO override for a specific product.
     *
     * @param productId the product UUID
     * @return the existing override
     * @throws SeoNotFoundException if no override exists for the given product
     */
    ProductSeoResponse getProductSeo(UUID productId);

    /**
     * Deletes the SEO override for a specific product, restoring the fallback to product data.
     *
     * @param productId the product UUID
     * @throws SeoNotFoundException if no override exists for the given product
     */
    void deleteProductSeo(UUID productId);

    /**
     * Returns the shop-wide SEO configuration for public (unauthenticated) consumers such as the buyer portal.
     * Returns an empty configuration if no record exists yet.
     *
     * @return the shop SEO configuration
     */
    ShopSeoResponse getPublicShopSeo();

    /**
     * Returns the SEO override for a product for public consumers.
     * Returns {@code null} if no override exists (the caller falls back to product data).
     *
     * @param productId the product UUID
     * @return the override, or {@code null} if none exists
     */
    ProductSeoResponse getPublicProductSeo(UUID productId);

    /**
     * Generates the {@code /sitemap.xml} content based on the current SEO and indexation settings.
     *
     * @param baseUrl the base URL of the buyer portal (e.g. {@code https://shop.example.com})
     * @return the full XML sitemap as a string
     */
    String generateSitemap(String baseUrl);

    /**
     * Generates the {@code /robots.txt} content based on the current SEO settings.
     *
     * @return the full robots.txt content as a string
     */
    String generateRobots();
}
