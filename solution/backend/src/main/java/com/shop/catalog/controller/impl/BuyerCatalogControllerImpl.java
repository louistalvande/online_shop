package com.shop.catalog.controller.impl;

import com.shop.catalog.controller.BuyerCatalogController;
import com.shop.catalog.dto.BuyerProductResponse;
import com.shop.catalog.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

/** {@link BuyerCatalogController} implementation. */
@RestController
public class BuyerCatalogControllerImpl implements BuyerCatalogController {

    private final ProductService productService;

    /**
     * Constructs the controller with its required service.
     *
     * @param productService the product service
     */
    public BuyerCatalogControllerImpl(ProductService productService) {
        this.productService = productService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Page<BuyerProductResponse>> browseProducts(
            String category, BigDecimal maxPrice, boolean inStockOnly, String search, Pageable pageable) {
        return ResponseEntity.ok(productService.browseProducts(category, maxPrice, inStockOnly, search, pageable));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<BuyerProductResponse> getProduct(UUID id) {
        return ResponseEntity.ok(productService.getPublishedProduct(id));
    }
}
