package com.shop.catalog.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.repository.AccountRepository;
import com.shop.catalog.dto.BuyerProductResponse;
import com.shop.catalog.dto.CreateProductRequest;
import com.shop.catalog.dto.ProductResponse;
import com.shop.catalog.dto.StockAlertResponse;
import com.shop.catalog.dto.UpdateProductRequest;
import com.shop.catalog.dto.UpdateStockRequest;
import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductPhoto;
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.entity.StockAlert;
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
import java.util.List;
import java.util.UUID;

/** {@link ProductService} implementation. */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StockAlertRepository stockAlertRepository;
    private final AccountRepository accountRepository;

    /**
     * Constructs the service with its required repositories.
     *
     * @param productRepository    the product JPA repository
     * @param stockAlertRepository the stock alert JPA repository
     * @param accountRepository    the account JPA repository (used to resolve vendor UUID from email)
     */
    public ProductServiceImpl(ProductRepository productRepository,
                               StockAlertRepository stockAlertRepository,
                               AccountRepository accountRepository) {
        this.productRepository = productRepository;
        this.stockAlertRepository = stockAlertRepository;
        this.accountRepository = accountRepository;
    }

    /** {@inheritDoc} */
    @Override
    public ProductResponse createProduct(String vendorEmail, CreateProductRequest request) {
        UUID vendorId = resolveVendorId(vendorEmail);

        Product product = new Product();
        product.setVendorId(vendorId);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPriceExclTax(request.getPriceExclTax());
        product.setCategory(request.getCategory());
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
    public List<ProductResponse> listProducts(String vendorEmail) {
        UUID vendorId = resolveVendorId(vendorEmail);
        return productRepository.findByVendorIdOrderByCreatedAtDesc(vendorId).stream()
                .map(ProductResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(String vendorEmail, UUID productId) {
        UUID vendorId = resolveVendorId(vendorEmail);
        Product product = productRepository.findByIdAndVendorId(productId, vendorId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductResponse.from(product);
    }

    /** {@inheritDoc} */
    @Override
    public ProductResponse updateProduct(String vendorEmail, UUID productId, UpdateProductRequest request) {
        UUID vendorId = resolveVendorId(vendorEmail);
        Product product = productRepository.findByIdAndVendorId(productId, vendorId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPriceExclTax(request.getPriceExclTax());
        product.setCategory(request.getCategory());
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
    public ProductResponse archiveProduct(String vendorEmail, UUID productId) {
        UUID vendorId = resolveVendorId(vendorEmail);
        Product product = productRepository.findByIdAndVendorId(productId, vendorId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // TODO(US-ORD): block archiving when active orders reference this product
        // (orders domain not yet implemented — always proceeds for now)

        product.setStatus(ProductStatus.ARCHIVED);
        return ProductResponse.from(productRepository.save(product));
    }

    /** {@inheritDoc} */
    @Override
    public ProductResponse updateStock(String vendorEmail, UUID productId, UpdateStockRequest request) {
        UUID vendorId = resolveVendorId(vendorEmail);
        Product product = productRepository.findByIdAndVendorId(productId, vendorId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setQuantity(request.getQuantity());
        product.setStockAlertThreshold(request.getStockAlertThreshold());

        Product saved = productRepository.save(product);
        raiseAlertIfNeeded(saved);
        return ProductResponse.from(saved);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<StockAlertResponse> listPendingAlerts(String vendorEmail) {
        UUID vendorId = resolveVendorId(vendorEmail);
        return stockAlertRepository
                .findByProduct_VendorIdAndAcknowledgedOrderByTriggeredAtDesc(vendorId, false)
                .stream()
                .map(StockAlertResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public StockAlertResponse acknowledgeAlert(String vendorEmail, UUID alertId) {
        UUID vendorId = resolveVendorId(vendorEmail);
        StockAlert alert = stockAlertRepository.findById(alertId)
                .filter(a -> a.getProduct().getVendorId().equals(vendorId))
                .orElseThrow(() -> new ProductNotFoundException(alertId));
        alert.setAcknowledged(true);
        return StockAlertResponse.from(stockAlertRepository.save(alert));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<BuyerProductResponse> browseProducts(
            String category, BigDecimal maxPrice, boolean inStockOnly, String search, Pageable pageable) {
        Specification<Product> spec = Specification
                .where(ProductSpecifications.published())
                .and(ProductSpecifications.withCategory(category))
                .and(ProductSpecifications.withMaxPriceTTC(maxPrice))
                .and(ProductSpecifications.inStockOnly(inStockOnly))
                .and(ProductSpecifications.nameLike(search));
        return productRepository.findAll(spec, pageable).map(BuyerProductResponse::from);
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UUID resolveVendorId(String email) {
        return accountRepository.findByEmail(email)
                .map(Account::getId)
                .orElseThrow(() -> new AccountNotFoundException(email));
    }

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
