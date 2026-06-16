package com.shop.seo.service.impl;

import com.shop.catalog.entity.Product;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.repository.ProductRepository;
import com.shop.seo.dto.ProductSeoRequest;
import com.shop.seo.dto.ProductSeoResponse;
import com.shop.seo.dto.ShopSeoRequest;
import com.shop.seo.dto.ShopSeoResponse;
import com.shop.seo.entity.ProductSeoOverride;
import com.shop.seo.entity.ShopSeo;
import com.shop.seo.exception.SeoNotFoundException;
import com.shop.seo.repository.ProductSeoOverrideRepository;
import com.shop.seo.repository.ShopSeoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link SeoServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class SeoServiceImplTest {

    @Mock ShopSeoRepository shopSeoRepository;
    @Mock ProductSeoOverrideRepository productSeoOverrideRepository;
    @Mock ProductRepository productRepository;

    SeoServiceImpl service;

    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SeoServiceImpl(shopSeoRepository, productSeoOverrideRepository, productRepository);
    }

    // --- getShopSeo ---

    @Test
    void getShopSeo_returnsEmpty_whenNoRecord() {
        given(shopSeoRepository.findAll()).willReturn(Collections.emptyList());

        ShopSeoResponse result = service.getShopSeo();

        assertThat(result).isNotNull();
        assertThat(result.getSeoTitle()).isNull();
        assertThat(result.isIndexProducts()).isTrue();
    }

    @Test
    void getShopSeo_returnsExistingRecord() {
        ShopSeo entity = new ShopSeo();
        entity.setSeoTitle("My Shop");
        given(shopSeoRepository.findAll()).willReturn(List.of(entity));

        ShopSeoResponse result = service.getShopSeo();

        assertThat(result.getSeoTitle()).isEqualTo("My Shop");
    }

    // --- saveShopSeo ---

    @Test
    void saveShopSeo_createsNewRecord_whenNoneExists() {
        given(shopSeoRepository.findAll()).willReturn(Collections.emptyList());
        ShopSeo saved = new ShopSeo();
        saved.setSeoTitle("New Title");
        given(shopSeoRepository.save(any(ShopSeo.class))).willReturn(saved);

        ShopSeoRequest request = new ShopSeoRequest();
        request.setSeoTitle("New Title");
        ShopSeoResponse result = service.saveShopSeo(request);

        then(shopSeoRepository).should().save(any(ShopSeo.class));
        assertThat(result.getSeoTitle()).isEqualTo("New Title");
    }

    @Test
    void saveShopSeo_updatesExistingRecord() {
        ShopSeo existing = new ShopSeo();
        existing.setSeoTitle("Old Title");
        given(shopSeoRepository.findAll()).willReturn(List.of(existing));
        given(shopSeoRepository.save(any(ShopSeo.class))).willAnswer(inv -> inv.getArgument(0));

        ShopSeoRequest request = new ShopSeoRequest();
        request.setSeoTitle("Updated Title");
        service.saveShopSeo(request);

        then(shopSeoRepository).should().save(existing);
        assertThat(existing.getSeoTitle()).isEqualTo("Updated Title");
    }

    // --- saveProductSeo ---

    @Test
    void saveProductSeo_throwsProductNotFound_whenProductMissing() {
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        ProductSeoRequest request = new ProductSeoRequest();
        assertThatThrownBy(() -> service.saveProductSeo(PRODUCT_ID, request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void saveProductSeo_createsOverride() {
        Product product = new Product();
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
        given(productSeoOverrideRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());
        ProductSeoOverride saved = new ProductSeoOverride();
        saved.setProduct(product);
        saved.setSeoTitle("Custom Title");
        given(productSeoOverrideRepository.save(any(ProductSeoOverride.class))).willReturn(saved);

        ProductSeoRequest request = new ProductSeoRequest();
        request.setSeoTitle("Custom Title");
        ProductSeoResponse result = service.saveProductSeo(PRODUCT_ID, request);

        then(productSeoOverrideRepository).should().save(any(ProductSeoOverride.class));
        assertThat(result.getSeoTitle()).isEqualTo("Custom Title");
    }

    // --- getProductSeo ---

    @Test
    void getProductSeo_throwsSeoNotFound_whenNoOverride() {
        given(productSeoOverrideRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductSeo(PRODUCT_ID))
                .isInstanceOf(SeoNotFoundException.class);
    }

    @Test
    void getProductSeo_returnsOverride() {
        ProductSeoOverride override = new ProductSeoOverride();
        override.setSeoTitle("Product SEO");
        given(productSeoOverrideRepository.findById(PRODUCT_ID)).willReturn(Optional.of(override));

        ProductSeoResponse result = service.getProductSeo(PRODUCT_ID);

        assertThat(result.getSeoTitle()).isEqualTo("Product SEO");
    }

    // --- deleteProductSeo ---

    @Test
    void deleteProductSeo_throwsSeoNotFound_whenNoOverride() {
        given(productSeoOverrideRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteProductSeo(PRODUCT_ID))
                .isInstanceOf(SeoNotFoundException.class);
    }

    @Test
    void deleteProductSeo_deletesExistingOverride() {
        ProductSeoOverride override = new ProductSeoOverride();
        given(productSeoOverrideRepository.findById(PRODUCT_ID)).willReturn(Optional.of(override));

        service.deleteProductSeo(PRODUCT_ID);

        then(productSeoOverrideRepository).should().delete(override);
    }

    // --- generateSitemap ---

    @Test
    void generateSitemap_includesCatalogAndProducts_whenBothEnabled() {
        ShopSeo entity = new ShopSeo();
        entity.setIndexProducts(true);
        entity.setIndexCatalog(true);
        entity.setSitemapChangefreq("weekly");
        given(shopSeoRepository.findAll()).willReturn(List.of(entity));
        Product p = new Product();
        given(productRepository.findAll()).willReturn(List.of(p));

        String sitemap = service.generateSitemap("https://shop.example.com");

        assertThat(sitemap).contains("<urlset");
        assertThat(sitemap).contains("https://shop.example.com/catalog");
    }

    @Test
    void generateSitemap_excludesCatalog_whenIndexCatalogFalse() {
        ShopSeo entity = new ShopSeo();
        entity.setIndexProducts(false);
        entity.setIndexCatalog(false);
        given(shopSeoRepository.findAll()).willReturn(List.of(entity));

        String sitemap = service.generateSitemap("https://shop.example.com");

        assertThat(sitemap).doesNotContain("/catalog");
    }

    // --- generateRobots ---

    @Test
    void generateRobots_includesDisallowPaths() {
        ShopSeo entity = new ShopSeo();
        entity.setRobotsDisallowPaths("/cart\n/checkout");
        entity.setIndexProducts(true);
        entity.setIndexCatalog(true);
        given(shopSeoRepository.findAll()).willReturn(List.of(entity));

        String robots = service.generateRobots();

        assertThat(robots).contains("Disallow: /cart");
        assertThat(robots).contains("Disallow: /checkout");
        assertThat(robots).contains("Sitemap: /sitemap.xml");
    }

    @Test
    void generateRobots_disallowsCatalogProducts_whenIndexProductsFalse() {
        ShopSeo entity = new ShopSeo();
        entity.setIndexProducts(false);
        entity.setIndexCatalog(true);
        given(shopSeoRepository.findAll()).willReturn(List.of(entity));

        String robots = service.generateRobots();

        assertThat(robots).contains("Disallow: /catalog/");
    }
}
