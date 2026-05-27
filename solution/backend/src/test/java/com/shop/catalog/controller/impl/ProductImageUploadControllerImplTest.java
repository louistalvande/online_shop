package com.shop.catalog.controller.impl;

import com.shop.catalog.dto.ProductImageUploadResponse;
import com.shop.catalog.exception.UnsupportedProductImageTypeException;
import com.shop.catalog.service.ProductImageUploadService;
import com.shop.common.GlobalExceptionHandler;
import com.shop.common.JwtFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link ProductImageUploadControllerImpl}.
 * Verifies HTTP contract for the product image upload endpoint (US-CAT-09).
 */
@WebMvcTest(controllers = ProductImageUploadControllerImpl.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        })
class ProductImageUploadControllerImplTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductImageUploadService productImageUploadService;

    @MockBean
    JwtFilter jwtFilter;

    @MockBean
    GlobalExceptionHandler globalExceptionHandler;

    @Test
    void upload_validJpeg_returns200WithImageUrl() throws Exception {
        given(productImageUploadService.store(any()))
                .willReturn(new ProductImageUploadResponse("/uploads/products/abc.jpg"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "fake-data".getBytes());

        mockMvc.perform(multipart("/api/vendor/products/images").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/uploads/products/abc.jpg"));
    }

    @Test
    void upload_unsupportedType_returns400() throws Exception {
        given(productImageUploadService.store(any()))
                .willThrow(new UnsupportedProductImageTypeException("application/pdf"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf".getBytes());

        mockMvc.perform(multipart("/api/vendor/products/images").file(file))
                .andExpect(status().isBadRequest());
    }
}
