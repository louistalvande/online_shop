package com.shop.seo.controller;

import com.shop.seo.dto.ProductSeoRequest;
import com.shop.seo.dto.ProductSeoResponse;
import com.shop.seo.dto.ShopSeoRequest;
import com.shop.seo.dto.ShopSeoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Vendor endpoints for managing shop and product SEO configuration (US-SEO-01, US-SEO-02, US-SEO-04, US-SEO-05). */
@Tag(name = "Vendor SEO", description = "SEO configuration for the shop and individual products")
@RequestMapping("/api/vendor/seo")
public interface VendorSeoController {

    /**
     * Returns the current shop-wide SEO configuration.
     *
     * @return the SEO configuration
     */
    @Operation(summary = "Get shop-wide SEO configuration")
    @ApiResponse(responseCode = "200", description = "Configuration returned")
    @GetMapping
    ResponseEntity<ShopSeoResponse> getShopSeo();

    /**
     * Creates or replaces the shop-wide SEO configuration.
     *
     * @param request the new configuration
     * @return the persisted configuration
     */
    @Operation(summary = "Save shop-wide SEO configuration")
    @ApiResponse(responseCode = "200", description = "Configuration saved")
    @PutMapping
    ResponseEntity<ShopSeoResponse> saveShopSeo(@RequestBody ShopSeoRequest request);

    /**
     * Returns the SEO override for a specific product.
     *
     * @param productId the product UUID
     * @return the existing override
     */
    @Operation(summary = "Get SEO override for a product")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Override returned"),
        @ApiResponse(responseCode = "404", description = "No override found for this product")
    })
    @GetMapping("/products/{productId}")
    ResponseEntity<ProductSeoResponse> getProductSeo(@PathVariable UUID productId);

    /**
     * Creates or replaces the SEO override for a specific product.
     *
     * @param productId the product UUID
     * @param request   the override values
     * @return the persisted override
     */
    @Operation(summary = "Save SEO override for a product")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Override saved"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/products/{productId}")
    ResponseEntity<ProductSeoResponse> saveProductSeo(@PathVariable UUID productId,
                                                       @RequestBody ProductSeoRequest request);

    /**
     * Deletes the SEO override for a specific product.
     *
     * @param productId the product UUID
     * @return 204 No Content
     */
    @Operation(summary = "Delete SEO override for a product")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Override deleted"),
        @ApiResponse(responseCode = "404", description = "No override found for this product")
    })
    @DeleteMapping("/products/{productId}")
    ResponseEntity<Void> deleteProductSeo(@PathVariable UUID productId);
}
