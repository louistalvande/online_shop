package com.shop.catalog.controller.impl;

import com.shop.catalog.dto.BuyerProductResponse;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.service.ProductService;
import com.shop.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unit tests for {@link BuyerCatalogControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class BuyerCatalogControllerImplTest {

    @Mock ProductService productService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new BuyerCatalogControllerImpl(productService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private BuyerProductResponse buildResponse(UUID id, String name, boolean outOfStock) {
        BuyerProductResponse r = new BuyerProductResponse();
        setField(r, "id", id);
        setField(r, "name", name);
        setField(r, "priceExclTax", new BigDecimal("29.90"));
        setField(r, "priceTTC", new BigDecimal("35.88"));
        setField(r, "category", "Aquarelle");
        setField(r, "photoUrls", List.of());
        setField(r, "outOfStock", outOfStock);
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

    /** GET /buyer/products returns 200 with product page. */
    @Test
    void browseProducts_returns200_withPage() throws Exception {
        Page<BuyerProductResponse> page = new PageImpl<>(
                List.of(buildResponse(PRODUCT_ID, "Aquarelle forêt", false)),
                PageRequest.of(0, 20), 1);
        given(productService.browseProducts(isNull(), isNull(), eq(false), isNull(), any()))
                .willReturn(page);

        mvc.perform(get("/api/buyer/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Aquarelle forêt"))
                .andExpect(jsonPath("$.content[0].priceTTC").value(35.88))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    /** GET /buyer/products?category=Aquarelle filters by category. */
    @Test
    void browseProducts_returns200_withCategoryFilter() throws Exception {
        Page<BuyerProductResponse> page = new PageImpl<>(
                List.of(buildResponse(PRODUCT_ID, "Aquarelle forêt", false)),
                PageRequest.of(0, 20), 1);
        given(productService.browseProducts(eq("Aquarelle"), isNull(), eq(false), isNull(), any()))
                .willReturn(page);

        mvc.perform(get("/api/buyer/products?category=Aquarelle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("Aquarelle"));
    }

    /** GET /buyer/products?inStockOnly=true passes inStockOnly flag. */
    @Test
    void browseProducts_returns200_withInStockFilter() throws Exception {
        Page<BuyerProductResponse> empty = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        given(productService.browseProducts(isNull(), isNull(), eq(true), isNull(), any()))
                .willReturn(empty);

        mvc.perform(get("/api/buyer/products?inStockOnly=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    /** GET /buyer/products/{id} returns 200 when product exists. */
    @Test
    void getProduct_returns200_whenFound() throws Exception {
        given(productService.getPublishedProduct(PRODUCT_ID))
                .willReturn(buildResponse(PRODUCT_ID, "Aquarelle forêt", false));

        mvc.perform(get("/api/buyer/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Aquarelle forêt"));
    }

    /** GET /buyer/products/{id} returns 404 when product not found. */
    @Test
    void getProduct_returns404_whenNotFound() throws Exception {
        given(messageSource.getMessage(anyString(), any(), any(Locale.class))).willReturn("not found");
        given(productService.getPublishedProduct(PRODUCT_ID))
                .willThrow(new ProductNotFoundException(PRODUCT_ID));

        mvc.perform(get("/api/buyer/products/{id}", PRODUCT_ID))
                .andExpect(status().isNotFound());
    }
}
