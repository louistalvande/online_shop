package com.shop.seo.controller.impl;

import com.shop.seo.dto.ProductSeoRequest;
import com.shop.seo.dto.ProductSeoResponse;
import com.shop.seo.dto.ShopSeoRequest;
import com.shop.seo.dto.ShopSeoResponse;
import com.shop.seo.exception.SeoNotFoundException;
import com.shop.seo.service.SeoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

/** Unit tests for {@link VendorSeoControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class VendorSeoControllerImplTest {

    @Mock SeoService seoService;

    VendorSeoControllerImpl controller;

    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new VendorSeoControllerImpl(seoService);
    }

    // --- getShopSeo ---

    @Test
    void getShopSeo_returns200() {
        ShopSeoResponse dto = new ShopSeoResponse();
        given(seoService.getShopSeo()).willReturn(dto);

        ResponseEntity<ShopSeoResponse> response = controller.getShopSeo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    // --- saveShopSeo ---

    @Test
    void saveShopSeo_returns200_withSavedConfig() {
        ShopSeoRequest request = new ShopSeoRequest();
        ShopSeoResponse dto = new ShopSeoResponse();
        given(seoService.saveShopSeo(request)).willReturn(dto);

        ResponseEntity<ShopSeoResponse> response = controller.saveShopSeo(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(seoService).should().saveShopSeo(request);
    }

    // --- getProductSeo ---

    @Test
    void getProductSeo_returns200_whenOverrideExists() {
        ProductSeoResponse dto = new ProductSeoResponse();
        given(seoService.getProductSeo(PRODUCT_ID)).willReturn(dto);

        ResponseEntity<ProductSeoResponse> response = controller.getProductSeo(PRODUCT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getProductSeo_propagatesNotFound() {
        willThrow(new SeoNotFoundException(PRODUCT_ID)).given(seoService).getProductSeo(PRODUCT_ID);

        assertThatThrownBy(() -> controller.getProductSeo(PRODUCT_ID))
                .isInstanceOf(SeoNotFoundException.class);
    }

    // --- saveProductSeo ---

    @Test
    void saveProductSeo_returns200() {
        ProductSeoRequest request = new ProductSeoRequest();
        ProductSeoResponse dto = new ProductSeoResponse();
        given(seoService.saveProductSeo(PRODUCT_ID, request)).willReturn(dto);

        ResponseEntity<ProductSeoResponse> response = controller.saveProductSeo(PRODUCT_ID, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // --- deleteProductSeo ---

    @Test
    void deleteProductSeo_returns204() {
        ResponseEntity<Void> response = controller.deleteProductSeo(PRODUCT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(seoService).should().deleteProductSeo(PRODUCT_ID);
    }

    @Test
    void deleteProductSeo_propagatesNotFound() {
        willThrow(new SeoNotFoundException(PRODUCT_ID)).given(seoService).deleteProductSeo(PRODUCT_ID);

        assertThatThrownBy(() -> controller.deleteProductSeo(PRODUCT_ID))
                .isInstanceOf(SeoNotFoundException.class);
    }
}
