package com.shop.catalog.controller.impl;

import com.shop.catalog.dto.ProductResponse;
import com.shop.catalog.dto.StockAlertResponse;
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.service.ProductService;
import com.shop.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unit tests for {@link ProductControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class ProductControllerImplTest {

    @Mock ProductService productService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final String VENDOR_EMAIL = "vendor@example.com";
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    private final UsernamePasswordAuthenticationToken vendorPrincipal =
            new UsernamePasswordAuthenticationToken(VENDOR_EMAIL, null, List.of());

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new ProductControllerImpl(productService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    private ProductResponse buildProductResponse(UUID id, String name) {
        ProductResponse r = new ProductResponse();
        setField(r, "id", id);
        setField(r, "name", name);
        setField(r, "priceExclTax", new BigDecimal("29.90"));
        setField(r, "quantity", 10);
        setField(r, "stockAlertThreshold", 3);
        setField(r, "status", ProductStatus.PUBLISHED);
        setField(r, "photoUrls", List.of());
        setField(r, "outOfStock", false);
        setField(r, "belowThreshold", false);
        setField(r, "createdAt", LocalDateTime.now());
        setField(r, "updatedAt", LocalDateTime.now());
        return r;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** POST /products returns 201 with valid payload. */
    @Test
    void createProduct_returns201_whenValid() throws Exception {
        given(productService.createProduct(any()))
                .willReturn(buildProductResponse(PRODUCT_ID, "Aquarelle"));

        mvc.perform(post("/api/vendor/products")
                        .principal(vendorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Aquarelle",
                                  "priceExclTax": 29.90,
                                  "quantity": 10,
                                  "stockAlertThreshold": 3,
                                  "photoUrls": []
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Aquarelle"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    /** POST /products returns 400 when name is blank. */
    @Test
    void createProduct_returns400_whenNameBlank() throws Exception {
        mvc.perform(post("/api/vendor/products")
                        .principal(vendorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "priceExclTax": 29.90,
                                  "quantity": 10
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    /** POST /products returns 400 when price is missing. */
    @Test
    void createProduct_returns400_whenPriceMissing() throws Exception {
        mvc.perform(post("/api/vendor/products")
                        .principal(vendorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Aquarelle", "quantity": 10}
                                """))
                .andExpect(status().isBadRequest());
    }

    /** GET /products returns 200 with product list. */
    @Test
    void listProducts_returns200() throws Exception {
        given(productService.listProducts())
                .willReturn(List.of(buildProductResponse(PRODUCT_ID, "Aquarelle")));

        mvc.perform(get("/api/vendor/products").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aquarelle"));
    }

    /** GET /products/{id} returns 200 when product exists. */
    @Test
    void getProduct_returns200_whenFound() throws Exception {
        given(productService.getProduct(PRODUCT_ID))
                .willReturn(buildProductResponse(PRODUCT_ID, "Aquarelle"));

        mvc.perform(get("/api/vendor/products/" + PRODUCT_ID).principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aquarelle"));
    }

    /** GET /products/{id} returns 404 when product does not exist. */
    @Test
    void getProduct_returns404_whenNotFound() throws Exception {
        given(productService.getProduct(PRODUCT_ID))
                .willThrow(new ProductNotFoundException(PRODUCT_ID));
        given(messageSource.getMessage(eq("error.product.not.found"), isNull(), any(Locale.class)))
                .willReturn("Product not found.");

        mvc.perform(get("/api/vendor/products/" + PRODUCT_ID).principal(vendorPrincipal))
                .andExpect(status().isNotFound());
    }

    /** PUT /products/{id} returns 200 with updated product. */
    @Test
    void updateProduct_returns200_whenValid() throws Exception {
        given(productService.updateProduct(eq(PRODUCT_ID), any()))
                .willReturn(buildProductResponse(PRODUCT_ID, "Huile sur toile"));

        mvc.perform(put("/api/vendor/products/" + PRODUCT_ID)
                        .principal(vendorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Huile sur toile",
                                  "priceExclTax": 49.00,
                                  "quantity": 5,
                                  "stockAlertThreshold": 2,
                                  "photoUrls": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Huile sur toile"));
    }

    /** PATCH /products/{id}/archive returns 200. */
    @Test
    void archiveProduct_returns200() throws Exception {
        ProductResponse archived = buildProductResponse(PRODUCT_ID, "Aquarelle");
        setField(archived, "status", ProductStatus.ARCHIVED);
        given(productService.archiveProduct(PRODUCT_ID)).willReturn(archived);

        mvc.perform(patch("/api/vendor/products/" + PRODUCT_ID + "/archive").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    /** PATCH /products/{id}/archive returns 404 when product not found. */
    @Test
    void archiveProduct_returns404_whenNotFound() throws Exception {
        given(productService.archiveProduct(PRODUCT_ID))
                .willThrow(new ProductNotFoundException(PRODUCT_ID));
        given(messageSource.getMessage(eq("error.product.not.found"), isNull(), any(Locale.class)))
                .willReturn("Product not found.");

        mvc.perform(patch("/api/vendor/products/" + PRODUCT_ID + "/archive").principal(vendorPrincipal))
                .andExpect(status().isNotFound());
    }

    /** PATCH /products/{id}/stock returns 200 with updated stock. */
    @Test
    void updateStock_returns200_whenValid() throws Exception {
        given(productService.updateStock(eq(PRODUCT_ID), any()))
                .willReturn(buildProductResponse(PRODUCT_ID, "Aquarelle"));

        mvc.perform(patch("/api/vendor/products/" + PRODUCT_ID + "/stock")
                        .principal(vendorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 20, "stockAlertThreshold": 5}
                                """))
                .andExpect(status().isOk());
    }

    /** GET /alerts returns 200 with pending alerts. */
    @Test
    void listPendingAlerts_returns200() throws Exception {
        StockAlertResponse alert = new StockAlertResponse();
        setField(alert, "id", UUID.randomUUID());
        setField(alert, "productId", PRODUCT_ID);
        setField(alert, "productName", "Aquarelle");
        setField(alert, "quantity", 1);
        setField(alert, "stockAlertThreshold", 3);
        setField(alert, "triggeredAt", LocalDateTime.now());
        setField(alert, "acknowledged", false);
        given(productService.listPendingAlerts()).willReturn(List.of(alert));

        mvc.perform(get("/api/vendor/alerts").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Aquarelle"));
    }
}
