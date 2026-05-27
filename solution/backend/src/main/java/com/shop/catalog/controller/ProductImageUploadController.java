package com.shop.catalog.controller;

import com.shop.catalog.dto.ProductImageUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Vendor endpoint for uploading product images (US-CAT-09).
 * The uploaded file is stored server-side and a public URL is returned for inclusion
 * in the product's photo list.
 */
@Tag(name = "Vendor Catalog", description = "Product management for vendors")
@RequestMapping("/api/vendor/products/images")
public interface ProductImageUploadController {

    /**
     * Uploads a product image file, stores it server-side, and returns its public URL.
     * The returned URL should be passed in {@code photoUrls} when creating or updating a product.
     *
     * @param file the image file to upload (JPEG, PNG, GIF, or WebP)
     * @return the public URL of the stored image
     */
    @Operation(summary = "Upload a product image and receive its public URL")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Image stored — public URL returned"),
        @ApiResponse(responseCode = "400", description = "Unsupported file type"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not a vendor account")
    })
    @PostMapping(consumes = "multipart/form-data")
    ResponseEntity<ProductImageUploadResponse> upload(@RequestParam("file") MultipartFile file);
}
