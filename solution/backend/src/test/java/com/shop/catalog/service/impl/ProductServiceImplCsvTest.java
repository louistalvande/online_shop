package com.shop.catalog.service.impl;

import com.shop.catalog.dto.CsvImportResponse;
import com.shop.catalog.exception.CsvHeaderInvalidException;
import com.shop.catalog.repository.BackInStockSubscriptionRepository;
import com.shop.catalog.repository.ProductRepository;
import com.shop.catalog.repository.StockAlertRepository;
import com.shop.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/** Unit tests for the CSV import logic in {@link ProductServiceImpl} (US-CAT-06). */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplCsvTest {

    @Mock ProductRepository productRepository;
    @Mock StockAlertRepository stockAlertRepository;
    @Mock BackInStockSubscriptionRepository subscriptionRepository;
    @Mock NotificationService notificationService;

    ProductServiceImpl service;

    private static final String VALID_HEADER = "nom,description,prix,categorie,quantite,seuil_alerte";

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(
                productRepository, stockAlertRepository,
                subscriptionRepository, notificationService);
    }

    @Test
    void importProductsCsv_allValid_createsAllProducts() {
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        String csv = VALID_HEADER + "\n"
                + "Aquarelle forêt,Belle aquarelle,29.90,Aquarelle,10,3\n"
                + "Huile sur toile,,49.00,,5,0\n";

        CsvImportResponse result = service.importProductsCsv(csv);

        assertThat(result.getTotalCreated()).isEqualTo(2);
        assertThat(result.getTotalErrors()).isEqualTo(0);
        assertThat(result.getRows()).hasSize(2);
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("CREATED");
        assertThat(result.getRows().get(1).getStatus()).isEqualTo("CREATED");
    }

    @Test
    void importProductsCsv_partialErrors_importsValidRowsOnly() {
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        String csv = VALID_HEADER + "\n"
                + "Produit OK,,25.00,,0,0\n"
                + ",Description sans nom,10.00,,0,0\n"
                + "Prix invalide,,abc,,0,0\n"
                + "Produit 2,,15.00,,0,0\n";

        CsvImportResponse result = service.importProductsCsv(csv);

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
        String csv = "wrong,header,columns\nProduit,desc,10.00,,0,0\n";

        assertThatThrownBy(() -> service.importProductsCsv(csv))
                .isInstanceOf(CsvHeaderInvalidException.class);
    }

    @Test
    void importProductsCsv_emptyContent_throwsCsvHeaderInvalidException() {
        assertThatThrownBy(() -> service.importProductsCsv(""))
                .isInstanceOf(CsvHeaderInvalidException.class);
    }

    @Test
    void importProductsCsv_missingPrice_recordsError() {
        String csv = VALID_HEADER + "\nProduit sans prix,,,,,\n";

        CsvImportResponse result = service.importProductsCsv(csv);

        assertThat(result.getTotalErrors()).isEqualTo(1);
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("ERROR");
        assertThat(result.getRows().get(0).getMessage()).contains("prix");
    }

    @Test
    void importProductsCsv_negativePriceRow_recordsError() {
        String csv = VALID_HEADER + "\nProduit négatif,,-5.00,,0,0\n";

        CsvImportResponse result = service.importProductsCsv(csv);

        assertThat(result.getTotalErrors()).isEqualTo(1);
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("ERROR");
    }

    @Test
    void importProductsCsv_quotedFieldsWithComma_parsedCorrectly() {
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        String csv = VALID_HEADER + "\n\"Produit, spécial\",\"Beau, très beau\",19.90,,0,0\n";

        CsvImportResponse result = service.importProductsCsv(csv);

        assertThat(result.getTotalCreated()).isEqualTo(1);
        assertThat(result.getRows().get(0).getProduct().getName()).isEqualTo("Produit, spécial");
    }

    @Test
    void importProductsCsv_headerCaseInsensitive_accepted() {
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        String csv = "NOM,DESCRIPTION,PRIX,CATEGORIE,QUANTITE,SEUIL_ALERTE\nProduit A,,20.00,,0,0\n";

        CsvImportResponse result = service.importProductsCsv(csv);

        assertThat(result.getTotalCreated()).isEqualTo(1);
    }

    @Test
    void importProductsCsv_lineNumbersReflectCsvPosition() {
        String csv = VALID_HEADER + "\n,missing name,10.00,,0,0\n";

        CsvImportResponse result = service.importProductsCsv(csv);

        assertThat(result.getRows().get(0).getLineNumber()).isEqualTo(2);
    }
}
