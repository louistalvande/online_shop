package com.shop.catalog.controller.impl;

import com.shop.catalog.service.ProductService;
import com.shop.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unit tests for the CSV export endpoint of {@link ProductControllerImpl} (US-CAT-07). */
@ExtendWith(MockitoExtension.class)
class ProductExportControllerImplTest {

    @Mock ProductService productService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final String VENDOR_EMAIL = "vendor@example.com";

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
    void exportProducts_returns200WithCsvContentType() throws Exception {
        given(productService.exportProductsCsv())
                .willReturn("id,nom,description,prix,categorie,quantite,seuil_alerte,statut\n");

        mvc.perform(get("/api/vendor/products/export").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"));
    }

    @Test
    void exportProducts_contentDispositionHasDateStampedFilename() throws Exception {
        given(productService.exportProductsCsv())
                .willReturn("id,nom,description,prix,categorie,quantite,seuil_alerte,statut\n");

        mvc.perform(get("/api/vendor/products/export").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.matchesPattern(
                                "attachment; filename=\"catalogue_export_\\d{8}\\.csv\"")));
    }

    @Test
    void exportProducts_bodyStartsWithUtf8Bom() throws Exception {
        given(productService.exportProductsCsv())
                .willReturn("id,nom,description,prix,categorie,quantite,seuil_alerte,statut\n");

        byte[] body = mvc.perform(get("/api/vendor/products/export").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        // UTF-8 BOM: 0xEF 0xBB 0xBF
        assertThat(body[0]).isEqualTo((byte) 0xEF);
        assertThat(body[1]).isEqualTo((byte) 0xBB);
        assertThat(body[2]).isEqualTo((byte) 0xBF);
    }

    @Test
    void exportProducts_csvContentFollowsAfterBom() throws Exception {
        given(productService.exportProductsCsv())
                .willReturn("id,nom,description,prix,categorie,quantite,seuil_alerte,statut\nProduit,,10.00,,0,0,PUBLISHED\n");

        byte[] body = mvc.perform(get("/api/vendor/products/export").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        // Skip 3-byte BOM and decode
        String csv = new String(body, 3, body.length - 3, java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).startsWith("id,nom,description,");
        assertThat(csv).contains("Produit");
    }
}
