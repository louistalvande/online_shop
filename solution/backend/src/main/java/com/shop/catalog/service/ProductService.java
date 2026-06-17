package com.shop.catalog.service;

import com.shop.catalog.dto.BulkStockUpdateRequest;
import com.shop.catalog.dto.BulkStockUpdateResponse;
import com.shop.catalog.dto.BuyerProductResponse;
import com.shop.catalog.dto.CreateProductRequest;
import com.shop.catalog.dto.CsvImportResponse;
import com.shop.catalog.dto.ProductResponse;
import com.shop.catalog.dto.StockAlertResponse;
import com.shop.catalog.dto.UpdateProductRequest;
import com.shop.catalog.dto.UpdateStockRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Business logic for the vendor product catalog (US-CAT-01 to US-CAT-05). */
public interface ProductService {

    /**
     * Creates a new product in {@code PUBLISHED} status (US-CAT-01).
     *
     * @param request the product creation payload
     * @return the persisted product
     */
    ProductResponse createProduct(CreateProductRequest request);

    /**
     * Returns all products ordered by creation date descending (US-CAT-04).
     *
     * @return list of all products
     */
    List<ProductResponse> listProducts();

    /**
     * Returns a single product by ID.
     *
     * @param productId the product UUID
     * @return the product
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     */
    ProductResponse getProduct(UUID productId);

    /**
     * Updates all fields of an existing product (US-CAT-02).
     * If the updated quantity crosses the alert threshold, a stock alert is raised (US-CAT-05).
     *
     * @param productId the product UUID
     * @param request   the update payload
     * @return the updated product
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     */
    ProductResponse updateProduct(UUID productId, UpdateProductRequest request);

    /**
     * Archives a product so it no longer appears in the buyer catalog (US-CAT-03).
     *
     * @param productId the product UUID
     * @return the archived product
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     */
    ProductResponse archiveProduct(UUID productId);

    /**
     * Updates the stock quantity and alert threshold for a product (US-CAT-04).
     * If the new quantity crosses the alert threshold, a stock alert is raised (US-CAT-05).
     *
     * @param productId the product UUID
     * @param request   the stock update payload
     * @return the updated product
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     */
    ProductResponse updateStock(UUID productId, UpdateStockRequest request);

    /**
     * Returns all unacknowledged stock alerts (US-CAT-05).
     *
     * @return list of pending alerts ordered by triggered date descending
     */
    List<StockAlertResponse> listPendingAlerts();

    /**
     * Acknowledges a stock alert so it no longer appears in the pending list (US-CAT-05).
     *
     * @param alertId the alert UUID
     * @return the acknowledged alert
     */
    StockAlertResponse acknowledgeAlert(UUID alertId);

    /**
     * Returns a paginated list of published products with optional filters (US-SHP-01, US-SHP-02).
     *
     * @param category    optional exact category filter (case-insensitive)
     * @param maxPrice    optional maximum TTC price filter in euros
     * @param inStockOnly when true, excludes out-of-stock products
     * @param search      optional text search on product name (case-insensitive, partial match)
     * @param pageable    pagination and sort parameters
     * @return a page of buyer-facing product responses
     */
    Page<BuyerProductResponse> browseProducts(
            String category, String theme, BigDecimal maxPrice, boolean inStockOnly, String search, Pageable pageable);

    /**
     * Returns a single published product visible to buyers (US-SHP-01).
     *
     * @param slug the URL-friendly product slug
     * @return the buyer-facing product response
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     *         or is not published
     */
    BuyerProductResponse getPublishedProduct(String slug);

    /**
     * Returns all distinct non-blank product type values, sorted alphabetically (US-CAT-01).
     *
     * @return sorted list of existing product types for autocompletion
     */
    List<String> distinctProductTypes();

    /**
     * Returns all distinct non-blank product theme values, sorted alphabetically (US-CAT-01).
     *
     * @return sorted list of existing product themes for autocompletion
     */
    List<String> distinctProductThemes();

    /**
     * Returns all distinct non-blank product type values from published products,
     * sorted alphabetically. Used for buyer catalog autocomplete.
     *
     * @return sorted list of distinct published product types
     */
    List<String> distinctPublishedProductTypes();

    /**
     * Returns all distinct non-blank product theme values from published products,
     * sorted alphabetically. Used for buyer catalog autocomplete.
     *
     * @return sorted list of distinct published product themes
     */
    List<String> distinctPublishedProductThemes();

    /**
     * Updates stock quantity and alert threshold for multiple products in a single operation (US-CAT-08).
     * Each product is processed independently; failures for individual products do not prevent
     * successful updates for others (partial success).
     *
     * @param request the bulk stock update payload
     * @return per-product results with success and error counts
     */
    BulkStockUpdateResponse bulkUpdateStock(BulkStockUpdateRequest request);

    /**
     * Exports all products (published and archived) as a UTF-8 CSV string (US-CAT-07).
     * The CSV uses the header: {@code id,nom,description,prix,categorie,quantite,seuil_alerte,statut}.
     * Fields containing commas, newlines, or double-quotes are RFC 4180-quoted.
     *
     * @return the full CSV content as a UTF-8 string (without BOM — the caller adds it if needed)
     */
    String exportProductsCsv();

    /**
     * Imports products from a UTF-8 CSV string (US-CAT-06).
     * The CSV must have the header: {@code id,nom,description,prix,categorie,quantite,seuil_alerte}.
     * If the {@code id} column is non-blank the row performs a stock-only update on the existing product;
     * otherwise a new product is created.
     * Valid rows are imported even if other rows fail (partial import).
     *
     * @param csvContent the full CSV content decoded as UTF-8
     * @return per-row import results with totals
     * @throws com.shop.catalog.exception.CsvHeaderInvalidException if the header is missing or does
     *         not match the expected format
     */
    CsvImportResponse importProductsCsv(String csvContent);
}
