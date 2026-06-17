package com.shop.catalog.controller.impl;

import com.shop.catalog.controller.BuyerCatalogController;
import com.shop.catalog.dto.BuyerProductResponse;
import com.shop.catalog.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

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
            String category, String theme, BigDecimal maxPrice, boolean inStockOnly, String search, Pageable pageable) {
        return ResponseEntity.ok(productService.browseProducts(category, theme, maxPrice, inStockOnly, search, pageable));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<BuyerProductResponse> getProduct(String slug) {
        return ResponseEntity.ok(productService.getPublishedProduct(slug));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<String>> listDistinctTypes() {
        return ResponseEntity.ok(productService.distinctPublishedProductTypes());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<String>> listDistinctThemes() {
        return ResponseEntity.ok(productService.distinctPublishedProductThemes());
    }
}
