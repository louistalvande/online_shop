package com.shop.catalog.controller.impl;

import com.shop.catalog.dto.CsvImportResponse;
import com.shop.catalog.dto.CsvImportRowResult;
import com.shop.catalog.exception.CsvHeaderInvalidException;
import com.shop.catalog.service.ProductService;
import com.shop.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unit tests for the CSV import endpoint of {@link ProductControllerImpl} (US-CAT-06). */
@ExtendWith(MockitoExtension.class)
class ProductCsvImportControllerImplTest {

    @Mock ProductService productService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final String VENDOR_EMAIL = "vendor@example.com";
    private static final String VALID_HEADER = "nom,description,prix,categorie,quantite,seuil_alerte";

    private final UsernamePasswordAuthenticationToken vendorPrincipal =
            new UsernamePasswordAuthenticationToken(VENDOR_EMAIL, null, List.of());

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new ProductControllerImpl(productService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    @Test
    void importProducts_validCsv_returns200WithReport() throws Exception {
        String csvContent = VALID_HEADER + "\nAquarelle,,29.90,,10,3\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "products.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        CsvImportResponse response = new CsvImportResponse(List.of(), 1, 0);
        given(productService.importProductsCsv(any())).willReturn(response);

        mvc.perform(multipart("/api/vendor/products/import")
                        .file(file)
                        .principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCreated").value(1))
                .andExpect(jsonPath("$.totalErrors").value(0));
    }

    @Test
    void importProducts_invalidHeader_returns400() throws Exception {
        String csvContent = "bad,header\nProduit,10.00\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "products.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        given(productService.importProductsCsv(any()))
                .willThrow(new CsvHeaderInvalidException());
        given(messageSource.getMessage(eq("error.csv.header.invalid"), any(), any(Locale.class)))
                .willReturn("En-tête CSV invalide");

        mvc.perform(multipart("/api/vendor/products/import")
                        .file(file)
                        .principal(vendorPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CSV_HEADER_INVALID"));
    }

    @Test
    void importProducts_emptyFile_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        given(messageSource.getMessage(eq("error.csv.header.invalid"), any(), any(Locale.class)))
                .willReturn("En-tête CSV invalide");

        mvc.perform(multipart("/api/vendor/products/import")
                        .file(file)
                        .principal(vendorPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CSV_HEADER_INVALID"));
    }

    @Test
    void importProducts_partialErrors_returns200WithMixedResults() throws Exception {
        String csvContent = VALID_HEADER + "\nProduit OK,,10.00,,0,0\n,manque nom,5.00,,0,0\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "products.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        CsvImportResponse response = new CsvImportResponse(
                List.of(
                        CsvImportRowResult.created(2, null),
                        CsvImportRowResult.error(3, "Le nom est obligatoire")
                ), 1, 1);
        given(productService.importProductsCsv(any())).willReturn(response);

        mvc.perform(multipart("/api/vendor/products/import")
                        .file(file)
                        .principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCreated").value(1))
                .andExpect(jsonPath("$.totalErrors").value(1))
                .andExpect(jsonPath("$.rows[1].status").value("ERROR"));
    }
}
