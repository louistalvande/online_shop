package com.shop.catalog.service;

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
     * Creates a new product in {@code PUBLISHED} status for the given vendor (US-CAT-01).
     *
     * @param vendorEmail the email of the authenticated vendor
     * @param request     the product creation payload
     * @return the persisted product
     */
    ProductResponse createProduct(String vendorEmail, CreateProductRequest request);

    /**
     * Returns all products owned by the given vendor, ordered by creation date (US-CAT-04).
     *
     * @param vendorEmail the email of the authenticated vendor
     * @return list of the vendor's products
     */
    List<ProductResponse> listProducts(String vendorEmail);

    /**
     * Returns a single product owned by the given vendor.
     *
     * @param vendorEmail the email of the authenticated vendor
     * @param productId   the product UUID
     * @return the product
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     *         or does not belong to the vendor
     */
    ProductResponse getProduct(String vendorEmail, UUID productId);

    /**
     * Updates all fields of an existing product (US-CAT-02).
     * If the updated quantity crosses the alert threshold, a stock alert is raised (US-CAT-05).
     *
     * @param vendorEmail the email of the authenticated vendor
     * @param productId   the product UUID
     * @param request     the update payload
     * @return the updated product
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     *         or does not belong to the vendor
     */
    ProductResponse updateProduct(String vendorEmail, UUID productId, UpdateProductRequest request);

    /**
     * Archives a product so it no longer appears in the buyer catalog (US-CAT-03).
     *
     * @param vendorEmail the email of the authenticated vendor
     * @param productId   the product UUID
     * @return the archived product
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     *         or does not belong to the vendor
     * @throws com.shop.catalog.exception.ProductArchivedConflictException if the product is
     *         referenced in an active order
     */
    ProductResponse archiveProduct(String vendorEmail, UUID productId);

    /**
     * Updates the stock quantity and alert threshold for a product (US-CAT-04).
     * If the new quantity crosses the alert threshold, a stock alert is raised (US-CAT-05).
     *
     * @param vendorEmail the email of the authenticated vendor
     * @param productId   the product UUID
     * @param request     the stock update payload
     * @return the updated product
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     *         or does not belong to the vendor
     */
    ProductResponse updateStock(String vendorEmail, UUID productId, UpdateStockRequest request);

    /**
     * Returns unacknowledged stock alerts for the given vendor's products (US-CAT-05).
     *
     * @param vendorEmail the email of the authenticated vendor
     * @return list of pending alerts ordered by triggered date descending
     */
    List<StockAlertResponse> listPendingAlerts(String vendorEmail);

    /**
     * Acknowledges a stock alert so it no longer appears in the pending list (US-CAT-05).
     *
     * @param vendorEmail the email of the authenticated vendor
     * @param alertId     the alert UUID
     * @return the acknowledged alert
     */
    StockAlertResponse acknowledgeAlert(String vendorEmail, UUID alertId);

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
            String category, BigDecimal maxPrice, boolean inStockOnly, String search, Pageable pageable);

    /**
     * Returns a single published product visible to buyers (US-SHP-01).
     *
     * @param productId the product UUID
     * @return the buyer-facing product response
     * @throws com.shop.catalog.exception.ProductNotFoundException if the product does not exist
     *         or is not published
     */
    BuyerProductResponse getPublishedProduct(UUID productId);

    /**
     * Exports all the vendor's products (published and archived) as a UTF-8 CSV string (US-CAT-07).
     * The CSV uses the header: {@code nom,description,prix,categorie,quantite,seuil_alerte,statut}.
     * Fields containing commas, newlines, or double-quotes are RFC 4180-quoted.
     *
     * @param vendorEmail the email of the authenticated vendor
     * @return the full CSV content as a UTF-8 string (without BOM — the caller adds it if needed)
     */
    String exportProductsCsv(String vendorEmail);

    /**
     * Imports products from a UTF-8 CSV string (US-CAT-06).
     * The CSV must have the header: {@code nom,description,prix,categorie,quantite,seuil_alerte}.
     * Valid rows are imported even if other rows fail (partial import).
     *
     * @param vendorEmail the email of the authenticated vendor
     * @param csvContent  the full CSV content decoded as UTF-8
     * @return per-row import results with totals
     * @throws com.shop.catalog.exception.CsvHeaderInvalidException if the header is missing or does
     *         not match the expected format
     */
    CsvImportResponse importProductsCsv(String vendorEmail, String csvContent);
}
