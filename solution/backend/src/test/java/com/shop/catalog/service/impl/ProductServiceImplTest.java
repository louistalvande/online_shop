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
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.entity.StockAlert;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.repository.ProductRepository;
import com.shop.catalog.repository.StockAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link ProductServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock ProductRepository productRepository;
    @Mock StockAlertRepository stockAlertRepository;
    @Mock AccountRepository accountRepository;

    ProductServiceImpl service;

    private static final String VENDOR_EMAIL = "vendor@example.com";
    private static final UUID VENDOR_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(productRepository, stockAlertRepository, accountRepository);
    }

    private Account vendorAccount() {
        Account a = new Account();
        java.lang.reflect.Field idField;
        try {
            idField = Account.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(a, VENDOR_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        a.setEmail(VENDOR_EMAIL);
        return a;
    }

    private Product publishedProduct() {
        Product p = new Product();
        p.setVendorId(VENDOR_ID);
        p.setName("Aquarelle");
        p.setDescription("Belle aquarelle");
        p.setPriceExclTax(new BigDecimal("29.90"));
        p.setCategory("Aquarelle");
        p.setQuantity(10);
        p.setStockAlertThreshold(3);
        p.setStatus(ProductStatus.PUBLISHED);
        return p;
    }

    private CreateProductRequest createRequest(int quantity) {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("Aquarelle");
        req.setDescription("Belle aquarelle");
        req.setPriceExclTax(new BigDecimal("29.90"));
        req.setCategory("Aquarelle");
        req.setQuantity(quantity);
        req.setStockAlertThreshold(3);
        req.setPhotoUrls(List.of());
        return req;
    }

    /** createProduct must persist a PUBLISHED product and return its DTO. */
    @Test
    void createProduct_savesPublishedProductAndReturnsDto() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

        ProductResponse result = service.createProduct(VENDOR_EMAIL, createRequest(10));

        then(productRepository).should().save(any(Product.class));
        assertThat(result.getName()).isEqualTo("Aquarelle");
        assertThat(result.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
    }

    /** createProduct must raise a stock alert when quantity is below the threshold. */
    @Test
    void createProduct_raisesStockAlert_whenQuantityBelowThreshold() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));
        given(stockAlertRepository.existsByProduct_IdAndAcknowledged(any(), anyBoolean())).willReturn(false);

        service.createProduct(VENDOR_EMAIL, createRequest(1));

        then(stockAlertRepository).should().save(any(StockAlert.class));
    }

    /** createProduct must not raise a duplicate alert when one already exists. */
    @Test
    void createProduct_doesNotRaiseDuplicateAlert_whenAlertAlreadyPending() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));
        given(stockAlertRepository.existsByProduct_IdAndAcknowledged(any(), anyBoolean())).willReturn(true);

        service.createProduct(VENDOR_EMAIL, createRequest(1));

        then(stockAlertRepository).should(org.mockito.Mockito.never()).save(any(StockAlert.class));
    }

    /** createProduct must throw AccountNotFoundException when vendor email is unknown. */
    @Test
    void createProduct_throwsAccountNotFoundException_whenVendorUnknown() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.createProduct(VENDOR_EMAIL, createRequest(10)))
                .isInstanceOf(AccountNotFoundException.class);
    }

    /** listProducts must return all products owned by the vendor. */
    @Test
    void listProducts_returnsVendorProducts() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.findByVendorIdOrderByCreatedAtDesc(VENDOR_ID))
                .willReturn(List.of(publishedProduct(), publishedProduct()));

        List<ProductResponse> result = service.listProducts(VENDOR_EMAIL);

        assertThat(result).hasSize(2);
    }

    /** getProduct must return the product when it belongs to the vendor. */
    @Test
    void getProduct_returnsProduct_whenOwnedByVendor() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.findByIdAndVendorId(PRODUCT_ID, VENDOR_ID))
                .willReturn(Optional.of(publishedProduct()));

        ProductResponse result = service.getProduct(VENDOR_EMAIL, PRODUCT_ID);

        assertThat(result.getName()).isEqualTo("Aquarelle");
    }

    /** getProduct must throw ProductNotFoundException when product does not belong to vendor. */
    @Test
    void getProduct_throwsProductNotFoundException_whenNotOwned() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.findByIdAndVendorId(PRODUCT_ID, VENDOR_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProduct(VENDOR_EMAIL, PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class);
    }

    /** updateProduct must update all fields and return the updated DTO. */
    @Test
    void updateProduct_updatesFieldsAndReturnsDto() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.findByIdAndVendorId(PRODUCT_ID, VENDOR_ID))
                .willReturn(Optional.of(publishedProduct()));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

        UpdateProductRequest req = new UpdateProductRequest();
        req.setName("Huile sur toile");
        req.setDescription("Nouvelle description");
        req.setPriceExclTax(new BigDecimal("49.00"));
        req.setCategory("Huile");
        req.setQuantity(5);
        req.setStockAlertThreshold(2);
        req.setPhotoUrls(List.of());

        ProductResponse result = service.updateProduct(VENDOR_EMAIL, PRODUCT_ID, req);

        assertThat(result.getName()).isEqualTo("Huile sur toile");
        assertThat(result.getPriceExclTax()).isEqualByComparingTo("49.00");
    }

    /** archiveProduct must change status to ARCHIVED. */
    @Test
    void archiveProduct_setsStatusArchivedAndReturnsDto() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.findByIdAndVendorId(PRODUCT_ID, VENDOR_ID))
                .willReturn(Optional.of(publishedProduct()));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

        ProductResponse result = service.archiveProduct(VENDOR_EMAIL, PRODUCT_ID);

        assertThat(result.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
    }

    /** updateStock must update quantity and threshold then return the product. */
    @Test
    void updateStock_updatesQuantityAndThreshold() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.findByIdAndVendorId(PRODUCT_ID, VENDOR_ID))
                .willReturn(Optional.of(publishedProduct()));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

        UpdateStockRequest req = new UpdateStockRequest();
        req.setQuantity(20);
        req.setStockAlertThreshold(5);

        ProductResponse result = service.updateStock(VENDOR_EMAIL, PRODUCT_ID, req);

        assertThat(result.getQuantity()).isEqualTo(20);
        assertThat(result.getStockAlertThreshold()).isEqualTo(5);
    }

    /** listPendingAlerts must return only unacknowledged alerts for the vendor's products. */
    @Test
    void listPendingAlerts_returnsUnacknowledgedAlertsForVendor() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        Product product = publishedProduct();
        StockAlert alert = new StockAlert();
        alert.setProduct(product);
        alert.setTriggeredAt(LocalDateTime.now());
        given(stockAlertRepository.findByProduct_VendorIdAndAcknowledgedOrderByTriggeredAtDesc(
                VENDOR_ID, false)).willReturn(List.of(alert));

        List<StockAlertResponse> result = service.listPendingAlerts(VENDOR_EMAIL);

        assertThat(result).hasSize(1);
    }

    /** browseProducts must return buyer-facing DTO page from repository. */
    @Test
    @SuppressWarnings("unchecked")
    void browseProducts_returnsPageOfBuyerResponses() {
        Product p = publishedProduct();
        Page<Product> productPage = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);
        given(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .willReturn(productPage);

        Page<BuyerProductResponse> result = service.browseProducts(null, null, false, null, PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Aquarelle");
        assertThat(result.getContent().get(0).getPriceTTC())
                .isEqualByComparingTo(new BigDecimal("35.88"));
    }

    /** browseProducts must exclude out-of-stock products when inStockOnly is true (spec applied via repo). */
    @Test
    @SuppressWarnings("unchecked")
    void browseProducts_withInStockOnly_returnsEmptyPageWhenNoneInStock() {
        given(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .willReturn(Page.empty());

        Page<BuyerProductResponse> result = service.browseProducts(null, null, true, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
    }

    /** getPublishedProduct must return buyer DTO for a published product. */
    @Test
    void getPublishedProduct_returnsDto_whenPublished() {
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(publishedProduct()));

        BuyerProductResponse result = service.getPublishedProduct(PRODUCT_ID);

        assertThat(result.getName()).isEqualTo("Aquarelle");
        assertThat(result.isOutOfStock()).isFalse();
    }

    /** getPublishedProduct must throw when the product is archived. */
    @Test
    void getPublishedProduct_throws_whenArchived() {
        Product archived = publishedProduct();
        archived.setStatus(ProductStatus.ARCHIVED);
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(archived));

        assertThatThrownBy(() -> service.getPublishedProduct(PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class);
    }

    /** getPublishedProduct must throw when the product does not exist. */
    @Test
    void getPublishedProduct_throws_whenNotFound() {
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPublishedProduct(PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
