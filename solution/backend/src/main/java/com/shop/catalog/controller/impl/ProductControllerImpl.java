package com.shop.catalog.controller.impl;

import com.shop.catalog.controller.ProductController;
import com.shop.catalog.dto.CreateProductRequest;
import com.shop.catalog.dto.CsvImportResponse;
import com.shop.catalog.dto.ProductResponse;
import com.shop.catalog.dto.StockAlertResponse;
import com.shop.catalog.dto.UpdateProductRequest;
import com.shop.catalog.dto.UpdateStockRequest;
import com.shop.catalog.exception.CsvHeaderInvalidException;
import com.shop.catalog.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** {@link ProductController} implementation. */
@RestController
public class ProductControllerImpl implements ProductController {

    private final ProductService productService;

    /**
     * Constructs the controller with its required service.
     *
     * @param productService the product catalog service
     */
    public ProductControllerImpl(ProductService productService) {
        this.productService = productService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductResponse> createProduct(Principal principal,
                                                          CreateProductRequest request) {
        ProductResponse created = productService.createProduct(principal.getName(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<ProductResponse>> listProducts(Principal principal) {
        return ResponseEntity.ok(productService.listProducts(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductResponse> getProduct(Principal principal, UUID id) {
        return ResponseEntity.ok(productService.getProduct(principal.getName(), id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductResponse> updateProduct(Principal principal,
                                                          UUID id,
                                                          UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(principal.getName(), id, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductResponse> archiveProduct(Principal principal, UUID id) {
        return ResponseEntity.ok(productService.archiveProduct(principal.getName(), id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductResponse> updateStock(Principal principal,
                                                        UUID id,
                                                        UpdateStockRequest request) {
        return ResponseEntity.ok(productService.updateStock(principal.getName(), id, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<StockAlertResponse>> listPendingAlerts(Principal principal) {
        return ResponseEntity.ok(productService.listPendingAlerts(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<StockAlertResponse> acknowledgeAlert(Principal principal, UUID alertId) {
        return ResponseEntity.ok(productService.acknowledgeAlert(principal.getName(), alertId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CsvImportResponse> importProducts(Principal principal, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CsvHeaderInvalidException();
        }
        try {
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(productService.importProductsCsv(principal.getName(), csvContent));
        } catch (IOException e) {
            throw new CsvHeaderInvalidException();
        }
    }
}
