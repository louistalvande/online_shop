package com.shop.seo.controller;

import com.shop.seo.dto.ProductSeoResponse;
import com.shop.seo.dto.ShopSeoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Public (unauthenticated) endpoints for SEO data consumed by the buyer portal and crawlers (US-SEO-03, US-SEO-04). */
@Tag(name = "Public SEO", description = "Public SEO endpoints for the buyer portal and crawlers")
@RequestMapping("/api/public")
public interface PublicSeoController {

    /**
     * Returns the shop-wide SEO configuration for the buyer portal.
     *
     * @return the shop SEO configuration
     */
    @Operation(summary = "Get public shop SEO configuration")
    @ApiResponse(responseCode = "200", description = "Configuration returned")
    @GetMapping("/seo")
    ResponseEntity<ShopSeoResponse> getPublicShopSeo();

    /**
     * Returns the SEO override for a product, or 204 if none exists.
     *
     * @param productId the product UUID
     * @return the override or 204 No Content
     */
    @Operation(summary = "Get public SEO override for a product")
    @ApiResponse(responseCode = "200", description = "Override returned")
    @ApiResponse(responseCode = "204", description = "No override for this product")
    @GetMapping("/seo/products/{productId}")
    ResponseEntity<ProductSeoResponse> getPublicProductSeo(@PathVariable UUID productId);

    /**
     * Generates the XML sitemap for search engine crawlers.
     *
     * @param baseUrl optional base URL query parameter; defaults to the public shop URL
     * @return the sitemap XML
     */
    @Operation(summary = "Generate sitemap.xml")
    @ApiResponse(responseCode = "200", description = "Sitemap generated")
    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    ResponseEntity<String> getSitemap(@RequestParam(required = false, defaultValue = "") String baseUrl);

    /**
     * Generates the robots.txt for search engine crawlers.
     *
     * @return the robots.txt content
     */
    @Operation(summary = "Generate robots.txt")
    @ApiResponse(responseCode = "200", description = "robots.txt generated")
    @GetMapping(value = "/robots.txt", produces = "text/plain")
    ResponseEntity<String> getRobots();
}
