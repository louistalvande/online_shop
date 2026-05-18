package com.shop.catalog.controller;

import com.shop.catalog.dto.BuyerProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/** Public buyer-facing endpoints for browsing the product catalog (US-SHP-01, US-SHP-02). */
@Tag(name = "Buyer — Catalog", description = "Product catalog browsing for buyers and visitors")
@RequestMapping("/api/buyer")
public interface BuyerCatalogController {

    /**
     * Returns a paginated list of published products with optional filters (US-SHP-01, US-SHP-02).
     * Accessible without authentication.
     *
     * @param category    optional exact category filter (case-insensitive)
     * @param maxPrice    optional maximum TTC price in euros
     * @param inStockOnly when true, only in-stock products are returned (default: false)
     * @param search      optional text search on product name
     * @param pageable    pagination parameters ({@code page}, {@code size})
     * @return paginated list of buyer-facing products with HTTP 200
     */
    @Operation(summary = "Browse the product catalog")
    @ApiResponse(responseCode = "200", description = "Product page returned")
    @GetMapping("/products")
    ResponseEntity<Page<BuyerProductResponse>> browseProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @RequestParam(required = false) String search,
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable);

    /**
     * Returns a single published product (US-SHP-01).
     * Accessible without authentication.
     *
     * @param id the product UUID
     * @return the product with HTTP 200
     */
    @Operation(summary = "Get a published product")
    @ApiResponse(responseCode = "200", description = "Product returned")
    @ApiResponse(responseCode = "404", description = "Product not found or not published")
    @GetMapping("/products/{id}")
    ResponseEntity<BuyerProductResponse> getProduct(@PathVariable UUID id);
}
