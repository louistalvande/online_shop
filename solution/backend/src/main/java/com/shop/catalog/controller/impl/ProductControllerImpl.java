package com.shop.catalog.controller.impl;

import com.shop.catalog.controller.ProductController;
import com.shop.catalog.dto.BulkStockUpdateRequest;
import com.shop.catalog.dto.BulkStockUpdateResponse;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        ProductResponse created = productService.createProduct(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<ProductResponse>> listProducts(Principal principal) {
        return ResponseEntity.ok(productService.listProducts());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductResponse> getProduct(Principal principal, UUID id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductResponse> updateProduct(Principal principal,
                                                          UUID id,
                                                          UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductResponse> archiveProduct(Principal principal, UUID id) {
        return ResponseEntity.ok(productService.archiveProduct(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductResponse> updateStock(Principal principal,
                                                        UUID id,
                                                        UpdateStockRequest request) {
        return ResponseEntity.ok(productService.updateStock(id, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<StockAlertResponse>> listPendingAlerts(Principal principal) {
        return ResponseEntity.ok(productService.listPendingAlerts());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<StockAlertResponse> acknowledgeAlert(Principal principal, UUID alertId) {
        return ResponseEntity.ok(productService.acknowledgeAlert(alertId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<BulkStockUpdateResponse> bulkUpdateStock(Principal principal,
                                                                    BulkStockUpdateRequest request) {
        return ResponseEntity.ok(productService.bulkUpdateStock(request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> exportProducts(Principal principal) {
        String csv = productService.exportProductsCsv();
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, body, 0, bom.length);
        System.arraycopy(csvBytes, 0, body, bom.length, csvBytes.length);

        String filename = "catalogue_export_"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + ".csv";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(body);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CsvImportResponse> importProducts(Principal principal, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CsvHeaderInvalidException();
        }
        try {
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(productService.importProductsCsv(csvContent));
        } catch (IOException e) {
            throw new CsvHeaderInvalidException();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<String>> listDistinctTypes(Principal principal) {
        return ResponseEntity.ok(productService.distinctProductTypes());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<String>> listDistinctThemes(Principal principal) {
        return ResponseEntity.ok(productService.distinctProductThemes());
    }
}
