package com.shop.catalog.service.impl;

import com.shop.catalog.dto.BulkStockUpdateRequest;
import com.shop.catalog.dto.BulkStockUpdateResponse;
import com.shop.catalog.dto.BuyerProductResponse;
import com.shop.catalog.dto.CreateProductRequest;
import com.shop.catalog.dto.CsvImportResponse;
import com.shop.catalog.dto.CsvImportRowResult;
import com.shop.catalog.dto.ProductResponse;
import com.shop.catalog.dto.StockAlertResponse;
import com.shop.catalog.dto.UpdateProductRequest;
import com.shop.catalog.dto.UpdateStockRequest;
import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductPhoto;
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.entity.StockAlert;
import com.shop.catalog.exception.CsvHeaderInvalidException;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.repository.ProductRepository;
import com.shop.catalog.repository.ProductSpecifications;
import com.shop.catalog.repository.StockAlertRepository;
import com.shop.catalog.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** {@link ProductService} implementation. */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StockAlertRepository stockAlertRepository;

    /**
     * Constructs the service with its required repositories.
     *
     * @param productRepository    the product JPA repository
     * @param stockAlertRepository the stock alert JPA repository
     */
    public ProductServiceImpl(ProductRepository productRepository,
                               StockAlertRepository stockAlertRepository) {
        this.productRepository = productRepository;
        this.stockAlertRepository = stockAlertRepository;
    }

    /** {@inheritDoc} */
    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPriceExclTax(request.getPriceExclTax());
        product.setCategory(request.getCategory());
        product.setTheme(request.getTheme());
        product.setQuantity(request.getQuantity());
        product.setStockAlertThreshold(request.getStockAlertThreshold());
        product.setStatus(ProductStatus.PUBLISHED);

        applyPhotos(product, request.getPhotoUrls());

        Product saved = productRepository.save(product);
        raiseAlertIfNeeded(saved);
        return ProductResponse.from(saved);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductResponse.from(product);
    }

    /** {@inheritDoc} */
    @Override
    public ProductResponse updateProduct(UUID productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPriceExclTax(request.getPriceExclTax());
        product.setCategory(request.getCategory());
        product.setTheme(request.getTheme());
        product.setQuantity(request.getQuantity());
        product.setStockAlertThreshold(request.getStockAlertThreshold());

        product.getPhotos().clear();
        applyPhotos(product, request.getPhotoUrls());

        Product saved = productRepository.save(product);
        raiseAlertIfNeeded(saved);
        return ProductResponse.from(saved);
    }

    /** {@inheritDoc} */
    @Override
    public ProductResponse archiveProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStatus(ProductStatus.ARCHIVED);
        return ProductResponse.from(productRepository.save(product));
    }

    /** {@inheritDoc} */
    @Override
    public ProductResponse updateStock(UUID productId, UpdateStockRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setQuantity(request.getQuantity());
        product.setStockAlertThreshold(request.getStockAlertThreshold());

        Product saved = productRepository.save(product);
        raiseAlertIfNeeded(saved);
        return ProductResponse.from(saved);
    }

    /** {@inheritDoc} */
    @Override
    public BulkStockUpdateResponse bulkUpdateStock(BulkStockUpdateRequest request) {
        List<BulkStockUpdateResponse.StockUpdateResult> results = new ArrayList<>();
        int totalUpdated = 0;
        int totalErrors = 0;

        for (BulkStockUpdateRequest.StockUpdateItem item : request.getUpdates()) {
            try {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
                product.setQuantity(item.getQuantity());
                product.setStockAlertThreshold(item.getStockAlertThreshold());
                Product saved = productRepository.save(product);
                raiseAlertIfNeeded(saved);
                results.add(BulkStockUpdateResponse.StockUpdateResult.updated(
                        item.getProductId(), ProductResponse.from(saved)));
                totalUpdated++;
            } catch (Exception e) {
                results.add(BulkStockUpdateResponse.StockUpdateResult.error(
                        item.getProductId(), e.getMessage()));
                totalErrors++;
            }
        }

        return new BulkStockUpdateResponse(results, totalUpdated, totalErrors);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<StockAlertResponse> listPendingAlerts() {
        return stockAlertRepository
                .findByAcknowledgedOrderByTriggeredAtDesc(false)
                .stream()
                .map(StockAlertResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public StockAlertResponse acknowledgeAlert(UUID alertId) {
        StockAlert alert = stockAlertRepository.findById(alertId)
                .orElseThrow(() -> new ProductNotFoundException(alertId));
        alert.setAcknowledged(true);
        return StockAlertResponse.from(stockAlertRepository.save(alert));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<BuyerProductResponse> browseProducts(
            String category, String theme, BigDecimal maxPrice, boolean inStockOnly, String search, Pageable pageable) {
        Specification<Product> spec = Specification
                .where(ProductSpecifications.published())
                .and(ProductSpecifications.withCategory(category))
                .and(ProductSpecifications.withTheme(theme))
                .and(ProductSpecifications.withMaxPriceTTC(maxPrice))
                .and(ProductSpecifications.inStockOnly(inStockOnly))
                .and(ProductSpecifications.nameLike(search));
        return productRepository.findAll(spec, pageable).map(BuyerProductResponse::from);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<String> distinctProductTypes() {
        return productRepository.findDistinctTypes();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<String> distinctProductThemes() {
        return productRepository.findDistinctThemes();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public BuyerProductResponse getPublishedProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getStatus() == ProductStatus.PUBLISHED)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return BuyerProductResponse.from(product);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public String exportProductsCsv() {
        List<ProductResponse> products = listProducts();
        StringBuilder sb = new StringBuilder();
        sb.append("nom,description,prix,categorie,quantite,seuil_alerte,statut\n");
        for (ProductResponse p : products) {
            sb.append(csvField(p.getName())).append(',');
            sb.append(csvField(p.getDescription())).append(',');
            sb.append(p.getPriceExclTax().toPlainString()).append(',');
            sb.append(csvField(p.getCategory())).append(',');
            sb.append(p.getQuantity()).append(',');
            sb.append(p.getStockAlertThreshold()).append(',');
            sb.append(p.getStatus().name()).append('\n');
        }
        return sb.toString();
    }

    /**
     * Wraps a CSV field value in double quotes when it contains a comma, double-quote, or newline.
     * Internal double-quotes are escaped as two double-quotes (RFC 4180).
     *
     * @param value the raw field value, possibly {@code null}
     * @return the safe CSV field representation
     */
    private static String csvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** Expected CSV header (exact match, case-insensitive). */
    private static final String EXPECTED_HEADER = "nom,description,prix,categorie,quantite,seuil_alerte";

    /** {@inheritDoc} */
    @Override
    public CsvImportResponse importProductsCsv(String csvContent) {
        String[] lines = csvContent.lines().toArray(String[]::new);

        int dataStartIndex = -1;
        String headerLine = null;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isBlank()) {
                headerLine = lines[i].strip();
                dataStartIndex = i + 1;
                break;
            }
        }
        if (headerLine == null || !EXPECTED_HEADER.equalsIgnoreCase(headerLine)) {
            throw new CsvHeaderInvalidException();
        }

        List<CsvImportRowResult> results = new ArrayList<>();
        int totalCreated = 0;
        int totalErrors = 0;

        for (int i = dataStartIndex; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) continue;
            int lineNumber = i + 1;

            try {
                List<String> fields = parseCsvLine(line);
                String nom         = fields.size() > 0 ? fields.get(0) : "";
                String description = fields.size() > 1 ? fields.get(1) : "";
                String prixStr     = fields.size() > 2 ? fields.get(2) : "";
                String categorie   = fields.size() > 3 ? fields.get(3) : "";
                String quantiteStr = fields.size() > 4 ? fields.get(4) : "";
                String seuilStr    = fields.size() > 5 ? fields.get(5) : "";

                if (nom.isBlank()) {
                    results.add(CsvImportRowResult.error(lineNumber, "Le nom est obligatoire"));
                    totalErrors++;
                    continue;
                }
                if (prixStr.isBlank()) {
                    results.add(CsvImportRowResult.error(lineNumber, "Le prix est obligatoire"));
                    totalErrors++;
                    continue;
                }
                BigDecimal prix;
                try {
                    prix = new BigDecimal(prixStr.replace(',', '.'));
                } catch (NumberFormatException e) {
                    results.add(CsvImportRowResult.error(lineNumber, "Prix invalide : " + prixStr));
                    totalErrors++;
                    continue;
                }
                if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                    results.add(CsvImportRowResult.error(lineNumber, "Le prix doit être supérieur à 0"));
                    totalErrors++;
                    continue;
                }

                int quantite = 0;
                if (!quantiteStr.isBlank()) {
                    try {
                        quantite = Integer.parseInt(quantiteStr);
                    } catch (NumberFormatException e) {
                        results.add(CsvImportRowResult.error(lineNumber, "Quantité invalide : " + quantiteStr));
                        totalErrors++;
                        continue;
                    }
                    if (quantite < 0) {
                        results.add(CsvImportRowResult.error(lineNumber, "La quantité ne peut pas être négative"));
                        totalErrors++;
                        continue;
                    }
                }

                int seuil = 0;
                if (!seuilStr.isBlank()) {
                    try {
                        seuil = Integer.parseInt(seuilStr);
                    } catch (NumberFormatException e) {
                        results.add(CsvImportRowResult.error(lineNumber, "Seuil d'alerte invalide : " + seuilStr));
                        totalErrors++;
                        continue;
                    }
                    if (seuil < 0) {
                        results.add(CsvImportRowResult.error(lineNumber, "Le seuil d'alerte ne peut pas être négatif"));
                        totalErrors++;
                        continue;
                    }
                }

                Product product = new Product();
                product.setName(nom);
                product.setDescription(description.isBlank() ? null : description);
                product.setPriceExclTax(prix);
                product.setCategory(categorie.isBlank() ? null : categorie);
                product.setQuantity(quantite);
                product.setStockAlertThreshold(seuil);
                product.setStatus(ProductStatus.PUBLISHED);

                Product saved = productRepository.save(product);
                raiseAlertIfNeeded(saved);
                results.add(CsvImportRowResult.created(lineNumber, ProductResponse.from(saved)));
                totalCreated++;

            } catch (Exception e) {
                results.add(CsvImportRowResult.error(lineNumber, "Erreur inattendue : " + e.getMessage()));
                totalErrors++;
            }
        }

        return new CsvImportResponse(results, totalCreated, totalErrors);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void applyPhotos(Product product, List<String> photoUrls) {
        for (int i = 0; i < photoUrls.size(); i++) {
            ProductPhoto photo = new ProductPhoto();
            photo.setProduct(product);
            photo.setUrl(photoUrls.get(i));
            photo.setSortOrder(i);
            product.getPhotos().add(photo);
        }
    }

    /**
     * Parses a single CSV line into fields, handling RFC 4180 quoted fields.
     * Surrounding whitespace on each field is stripped.
     *
     * @param line a single CSV data line
     * @return the ordered list of field values
     */
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().strip());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().strip());
        return fields;
    }

    /**
     * Creates a stock alert if the product quantity is below the alert threshold and no
     * unacknowledged alert already exists for this product (avoids duplicate alerts).
     *
     * @param product the product to check
     */
    private void raiseAlertIfNeeded(Product product) {
        if (product.getStockAlertThreshold() <= 0) {
            return;
        }
        if (product.getQuantity() >= product.getStockAlertThreshold()) {
            return;
        }
        boolean alreadyPending = stockAlertRepository
                .existsByProduct_IdAndAcknowledged(product.getId(), false);
        if (alreadyPending) {
            return;
        }
        StockAlert alert = new StockAlert();
        alert.setProduct(product);
        alert.setTriggeredAt(LocalDateTime.now());
        stockAlertRepository.save(alert);
    }
}
