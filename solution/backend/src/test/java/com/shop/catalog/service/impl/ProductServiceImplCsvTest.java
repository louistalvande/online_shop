package com.shop.catalog.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.repository.AccountRepository;
import com.shop.catalog.dto.CsvImportResponse;
import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.exception.CsvHeaderInvalidException;
import com.shop.catalog.repository.ProductRepository;
import com.shop.catalog.repository.StockAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;

/** Unit tests for the CSV import logic in {@link ProductServiceImpl} (US-CAT-06). */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplCsvTest {

    @Mock ProductRepository productRepository;
    @Mock StockAlertRepository stockAlertRepository;
    @Mock AccountRepository accountRepository;

    ProductServiceImpl service;

    private static final String VENDOR_EMAIL = "vendor@example.com";
    private static final UUID VENDOR_ID = UUID.randomUUID();

    private static final String VALID_HEADER = "nom,description,prix,categorie,quantite,seuil_alerte";

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(productRepository, stockAlertRepository, accountRepository);
    }

    private Account vendorAccount() {
        Account a = new Account();
        a.setId(VENDOR_ID);
        a.setEmail(VENDOR_EMAIL);
        return a;
    }

    private Product savedProduct(String name, BigDecimal price) {
        Product p = new Product();
        p.setVendorId(VENDOR_ID);
        p.setName(name);
        p.setPriceExclTax(price);
        p.setQuantity(0);
        p.setStockAlertThreshold(0);
        p.setStatus(ProductStatus.PUBLISHED);
        try {
            var field = Product.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(p, LocalDateTime.now());
            var field2 = Product.class.getDeclaredField("updatedAt");
            field2.setAccessible(true);
            field2.set(p, LocalDateTime.now());
        } catch (Exception ignored) { }
        return p;
    }

    @Test
    void importProductsCsv_allValid_createsAllProducts() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(stockAlertRepository.existsByProduct_IdAndAcknowledged(any(), anyBoolean())).willReturn(false);

        String csv = VALID_HEADER + "\n"
                + "Aquarelle forêt,Belle aquarelle,29.90,Aquarelle,10,3\n"
                + "Huile sur toile,,49.00,,5,0\n";

        CsvImportResponse result = service.importProductsCsv(VENDOR_EMAIL, csv);

        assertThat(result.getTotalCreated()).isEqualTo(2);
        assertThat(result.getTotalErrors()).isEqualTo(0);
        assertThat(result.getRows()).hasSize(2);
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("CREATED");
        assertThat(result.getRows().get(1).getStatus()).isEqualTo("CREATED");
    }

    @Test
    void importProductsCsv_partialErrors_importsValidRowsOnly() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(stockAlertRepository.existsByProduct_IdAndAcknowledged(any(), anyBoolean())).willReturn(false);

        String csv = VALID_HEADER + "\n"
                + "Produit OK,,25.00,,0,0\n"   // valid
                + ",Description sans nom,10.00,,0,0\n"   // missing name
                + "Prix invalide,,abc,,0,0\n"   // bad price
                + "Produit 2,,15.00,,0,0\n";    // valid

        CsvImportResponse result = service.importProductsCsv(VENDOR_EMAIL, csv);

        assertThat(result.getTotalCreated()).isEqualTo(2);
        assertThat(result.getTotalErrors()).isEqualTo(2);
        assertThat(result.getRows()).hasSize(4);
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("CREATED");
        assertThat(result.getRows().get(1).getStatus()).isEqualTo("ERROR");
        assertThat(result.getRows().get(2).getStatus()).isEqualTo("ERROR");
        assertThat(result.getRows().get(3).getStatus()).isEqualTo("CREATED");
    }

    @Test
    void importProductsCsv_invalidHeader_throwsCsvHeaderInvalidException() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));

        String csv = "wrong,header,columns\nProduit,desc,10.00,,0,0\n";

        assertThatThrownBy(() -> service.importProductsCsv(VENDOR_EMAIL, csv))
                .isInstanceOf(CsvHeaderInvalidException.class);
    }

    @Test
    void importProductsCsv_emptyContent_throwsCsvHeaderInvalidException() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));

        assertThatThrownBy(() -> service.importProductsCsv(VENDOR_EMAIL, ""))
                .isInstanceOf(CsvHeaderInvalidException.class);
    }

    @Test
    void importProductsCsv_missingPrice_recordsError() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));

        String csv = VALID_HEADER + "\nProduit sans prix,,,,,\n";

        CsvImportResponse result = service.importProductsCsv(VENDOR_EMAIL, csv);

        assertThat(result.getTotalErrors()).isEqualTo(1);
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("ERROR");
        assertThat(result.getRows().get(0).getMessage()).contains("prix");
    }

    @Test
    void importProductsCsv_negativePriceRow_recordsError() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));

        String csv = VALID_HEADER + "\nProduit négatif,,-5.00,,0,0\n";

        CsvImportResponse result = service.importProductsCsv(VENDOR_EMAIL, csv);

        assertThat(result.getTotalErrors()).isEqualTo(1);
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("ERROR");
    }

    @Test
    void importProductsCsv_quotedFieldsWithComma_parsedCorrectly() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(stockAlertRepository.existsByProduct_IdAndAcknowledged(any(), anyBoolean())).willReturn(false);

        // Description contains a comma inside quotes
        String csv = VALID_HEADER + "\n\"Produit, spécial\",\"Beau, très beau\",19.90,,0,0\n";

        CsvImportResponse result = service.importProductsCsv(VENDOR_EMAIL, csv);

        assertThat(result.getTotalCreated()).isEqualTo(1);
        assertThat(result.getRows().get(0).getProduct().getName()).isEqualTo("Produit, spécial");
    }

    @Test
    void importProductsCsv_headerCaseInsensitive_accepted() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(stockAlertRepository.existsByProduct_IdAndAcknowledged(any(), anyBoolean())).willReturn(false);

        String csv = "NOM,DESCRIPTION,PRIX,CATEGORIE,QUANTITE,SEUIL_ALERTE\nProduit A,,20.00,,0,0\n";

        CsvImportResponse result = service.importProductsCsv(VENDOR_EMAIL, csv);

        assertThat(result.getTotalCreated()).isEqualTo(1);
    }

    @Test
    void importProductsCsv_lineNumbersReflectCsvPosition() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendorAccount()));

        String csv = VALID_HEADER + "\n,missing name,10.00,,0,0\n";

        CsvImportResponse result = service.importProductsCsv(VENDOR_EMAIL, csv);

        // Header = line 1, data row = line 2
        assertThat(result.getRows().get(0).getLineNumber()).isEqualTo(2);
    }
}
