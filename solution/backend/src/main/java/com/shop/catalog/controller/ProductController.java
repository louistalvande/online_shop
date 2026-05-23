package com.shop.catalog.controller;

import com.shop.catalog.dto.CreateProductRequest;
import com.shop.catalog.dto.CsvImportResponse;
import com.shop.catalog.dto.ProductResponse;
import com.shop.catalog.dto.StockAlertResponse;
import com.shop.catalog.dto.UpdateProductRequest;
import com.shop.catalog.dto.UpdateStockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** Vendor endpoints for managing the product catalog (US-CAT-01 to US-CAT-05). */
@Tag(name = "Vendor — Catalog", description = "Product and stock management for vendors")
@RequestMapping("/api/vendor")
public interface ProductController {

    /**
     * Creates a new product in {@code PUBLISHED} status for the authenticated vendor (US-CAT-01).
     *
     * @param principal the authenticated vendor principal
     * @param request   the product creation payload
     * @return the created product with HTTP 201 and Location header
     */
    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping("/products")
    ResponseEntity<ProductResponse> createProduct(Principal principal,
                                                   @Valid @RequestBody CreateProductRequest request);

    /**
     * Returns all products owned by the authenticated vendor (US-CAT-04).
     *
     * @param principal the authenticated vendor principal
     * @return list of the vendor's products with HTTP 200
     */
    @Operation(summary = "List vendor products")
    @ApiResponse(responseCode = "200", description = "Product list returned")
    @GetMapping("/products")
    ResponseEntity<List<ProductResponse>> listProducts(Principal principal);

    /**
     * Returns a single product owned by the authenticated vendor.
     *
     * @param principal the authenticated vendor principal
     * @param id        the product UUID
     * @return the product with HTTP 200
     */
    @Operation(summary = "Get a product")
    @ApiResponse(responseCode = "200", description = "Product returned")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/products/{id}")
    ResponseEntity<ProductResponse> getProduct(Principal principal, @PathVariable UUID id);

    /**
     * Updates all fields of an existing product (US-CAT-02).
     *
     * @param principal the authenticated vendor principal
     * @param id        the product UUID
     * @param request   the update payload
     * @return the updated product with HTTP 200
     */
    @Operation(summary = "Update a product")
    @ApiResponse(responseCode = "200", description = "Product updated")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PutMapping("/products/{id}")
    ResponseEntity<ProductResponse> updateProduct(Principal principal,
                                                   @PathVariable UUID id,
                                                   @Valid @RequestBody UpdateProductRequest request);

    /**
     * Archives a product so it no longer appears in the buyer catalog (US-CAT-03).
     *
     * @param principal the authenticated vendor principal
     * @param id        the product UUID
     * @return the archived product with HTTP 200
     */
    @Operation(summary = "Archive a product")
    @ApiResponse(responseCode = "200", description = "Product archived")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "409", description = "Product referenced in an active order")
    @PatchMapping("/products/{id}/archive")
    ResponseEntity<ProductResponse> archiveProduct(Principal principal, @PathVariable UUID id);

    /**
     * Updates the stock quantity and alert threshold for a product (US-CAT-04).
     *
     * @param principal the authenticated vendor principal
     * @param id        the product UUID
     * @param request   the stock update payload
     * @return the updated product with HTTP 200
     */
    @Operation(summary = "Update product stock")
    @ApiResponse(responseCode = "200", description = "Stock updated")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PatchMapping("/products/{id}/stock")
    ResponseEntity<ProductResponse> updateStock(Principal principal,
                                                 @PathVariable UUID id,
                                                 @Valid @RequestBody UpdateStockRequest request);

    /**
     * Returns pending (unacknowledged) stock alerts for the vendor's products (US-CAT-05).
     *
     * @param principal the authenticated vendor principal
     * @return list of pending alerts with HTTP 200
     */
    @Operation(summary = "List pending stock alerts")
    @ApiResponse(responseCode = "200", description = "Alert list returned")
    @GetMapping("/alerts")
    ResponseEntity<List<StockAlertResponse>> listPendingAlerts(Principal principal);

    /**
     * Acknowledges a stock alert so it is removed from the pending list (US-CAT-05).
     *
     * @param principal the authenticated vendor principal
     * @param alertId   the alert UUID
     * @return the acknowledged alert with HTTP 200
     */
    @Operation(summary = "Acknowledge a stock alert")
    @ApiResponse(responseCode = "200", description = "Alert acknowledged")
    @ApiResponse(responseCode = "404", description = "Alert not found")
    @PatchMapping("/alerts/{alertId}/acknowledge")
    ResponseEntity<StockAlertResponse> acknowledgeAlert(Principal principal, @PathVariable UUID alertId);

    /**
     * Imports products from a UTF-8 CSV file (US-CAT-06).
     * Valid rows are created even if other rows fail (partial import).
     * Returns 400 when the file is empty or the CSV header is invalid.
     *
     * @param principal the authenticated vendor principal
     * @param file      the uploaded CSV file
     * @return per-row import results with totals, HTTP 200
     */
    @Operation(summary = "Import products from a CSV file")
    @ApiResponse(responseCode = "200", description = "Import processed — may contain row-level errors")
    @ApiResponse(responseCode = "400", description = "Empty file or invalid CSV header")
    @PostMapping(value = "/products/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<CsvImportResponse> importProducts(Principal principal,
                                                     @RequestParam("file") MultipartFile file);
}
