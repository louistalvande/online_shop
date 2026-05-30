package com.shop.catalog.service.impl;

import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.repository.BackInStockSubscriptionRepository;
import com.shop.catalog.repository.ProductRepository;
import com.shop.catalog.repository.StockAlertRepository;
import com.shop.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/** Unit tests for the CSV export logic in {@link ProductServiceImpl} (US-CAT-07). */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplExportTest {

    @Mock ProductRepository productRepository;
    @Mock StockAlertRepository stockAlertRepository;
    @Mock BackInStockSubscriptionRepository subscriptionRepository;
    @Mock NotificationService notificationService;

    ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(
                productRepository, stockAlertRepository,
                subscriptionRepository, notificationService);
    }

    private Product buildProduct(String name, String description, BigDecimal price,
                                  String category, int qty, int threshold, ProductStatus status) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPriceExclTax(price);
        p.setCategory(category);
        p.setQuantity(qty);
        p.setStockAlertThreshold(threshold);
        p.setStatus(status);
        try {
            var f1 = Product.class.getDeclaredField("createdAt");
            f1.setAccessible(true);
            f1.set(p, LocalDateTime.now());
            var f2 = Product.class.getDeclaredField("updatedAt");
            f2.setAccessible(true);
            f2.set(p, LocalDateTime.now());
        } catch (Exception ignored) { }
        return p;
    }

    @Test
    void exportProductsCsv_returnsHeaderAndOneRowPerProduct() {
        given(productRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(
                buildProduct("Aquarelle forêt", "Belle aquarelle", new BigDecimal("29.90"),
                        "Aquarelle", 10, 3, ProductStatus.PUBLISHED)
        ));

        String csv = service.exportProductsCsv();

        String[] lines = csv.split("\n");
        assertThat(lines[0]).isEqualTo("nom,description,prix,categorie,quantite,seuil_alerte,statut");
        assertThat(lines[1]).contains("Aquarelle forêt");
        assertThat(lines[1]).contains("29.90");
        assertThat(lines[1]).contains("PUBLISHED");
    }

    @Test
    void exportProductsCsv_includesArchivedProducts() {
        given(productRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(
                buildProduct("Publié", null, new BigDecimal("10.00"), null, 5, 0, ProductStatus.PUBLISHED),
                buildProduct("Archivé", null, new BigDecimal("20.00"), null, 0, 0, ProductStatus.ARCHIVED)
        ));

        String csv = service.exportProductsCsv();

        assertThat(csv).contains("PUBLISHED");
        assertThat(csv).contains("ARCHIVED");
        long dataLines = csv.lines().filter(l -> !l.startsWith("nom,")).count();
        assertThat(dataLines).isEqualTo(2);
    }

    @Test
    void exportProductsCsv_emptyVendorCatalog_returnsHeaderOnly() {
        given(productRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of());

        String csv = service.exportProductsCsv();

        assertThat(csv.strip()).isEqualTo("nom,description,prix,categorie,quantite,seuil_alerte,statut");
    }

    @Test
    void exportProductsCsv_fieldWithComma_isQuoted() {
        given(productRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(
                buildProduct("Produit, spécial", "Aquarelle, très belle", new BigDecimal("15.00"),
                        null, 0, 0, ProductStatus.PUBLISHED)
        ));

        String csv = service.exportProductsCsv();

        assertThat(csv).contains("\"Produit, spécial\"");
        assertThat(csv).contains("\"Aquarelle, très belle\"");
    }

    @Test
    void exportProductsCsv_nullOptionalFields_exportedAsEmpty() {
        given(productRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(
                buildProduct("Produit minimal", null, new BigDecimal("5.00"), null, 0, 0, ProductStatus.PUBLISHED)
        ));

        String csv = service.exportProductsCsv();

        assertThat(csv).contains("Produit minimal,,5.00,,0,0,PUBLISHED");
    }
}
