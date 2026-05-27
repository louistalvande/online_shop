package com.shop.catalog.controller.impl;

import com.shop.catalog.dto.ProductImageUploadResponse;
import com.shop.catalog.exception.UnsupportedProductImageTypeException;
import com.shop.catalog.service.ProductImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

/** Unit tests for {@link ProductImageUploadControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class ProductImageUploadControllerImplTest {

    @Mock
    ProductImageUploadService productImageUploadService;

    ProductImageUploadControllerImpl controller;

    @BeforeEach
    void setUp() {
        controller = new ProductImageUploadControllerImpl(productImageUploadService);
    }

    @Test
    void upload_validFile_returns200WithImageUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "bytes".getBytes());
        given(productImageUploadService.store(file))
                .willReturn(new ProductImageUploadResponse("/uploads/products/abc.jpg"));

        ResponseEntity<ProductImageUploadResponse> response = controller.upload(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getImageUrl()).isEqualTo("/uploads/products/abc.jpg");
    }

    @Test
    void upload_unsupportedType_propagatesException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf".getBytes());
        willThrow(new UnsupportedProductImageTypeException("application/pdf"))
                .given(productImageUploadService).store(file);

        assertThatThrownBy(() -> controller.upload(file))
                .isInstanceOf(UnsupportedProductImageTypeException.class);
    }
}
