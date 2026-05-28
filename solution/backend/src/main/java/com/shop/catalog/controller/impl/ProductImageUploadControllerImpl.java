package com.shop.catalog.controller.impl;

import com.shop.catalog.controller.ProductImageUploadController;
import com.shop.catalog.dto.ProductImageUploadResponse;
import com.shop.catalog.service.ProductImageUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** {@link ProductImageUploadController} implementation. */
@RestController
public class ProductImageUploadControllerImpl implements ProductImageUploadController {

    private final ProductImageUploadService productImageUploadService;

    /**
     * Constructs the controller with the product image upload service.
     *
     * @param productImageUploadService the service responsible for storing product images
     */
    public ProductImageUploadControllerImpl(ProductImageUploadService productImageUploadService) {
        this.productImageUploadService = productImageUploadService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductImageUploadResponse> upload(MultipartFile file) {
        return ResponseEntity.ok(productImageUploadService.store(file));
    }
}
