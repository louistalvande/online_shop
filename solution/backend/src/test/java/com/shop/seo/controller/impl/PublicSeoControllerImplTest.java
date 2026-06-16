package com.shop.seo.controller.impl;

import com.shop.seo.dto.ProductSeoResponse;
import com.shop.seo.dto.ShopSeoResponse;
import com.shop.seo.service.SeoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/** Unit tests for {@link PublicSeoControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class PublicSeoControllerImplTest {

    @Mock SeoService seoService;

    PublicSeoControllerImpl controller;

    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new PublicSeoControllerImpl(seoService);
        ReflectionTestUtils.setField(controller, "defaultBaseUrl", "http://localhost:5173");
    }

    @Test
    void getPublicShopSeo_returns200() {
        ShopSeoResponse dto = new ShopSeoResponse();
        given(seoService.getPublicShopSeo()).willReturn(dto);

        ResponseEntity<ShopSeoResponse> response = controller.getPublicShopSeo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void getPublicProductSeo_returns200_whenOverrideExists() {
        ProductSeoResponse dto = new ProductSeoResponse();
        given(seoService.getPublicProductSeo(PRODUCT_ID)).willReturn(dto);

        ResponseEntity<ProductSeoResponse> response = controller.getPublicProductSeo(PRODUCT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getPublicProductSeo_returns204_whenNoOverride() {
        given(seoService.getPublicProductSeo(PRODUCT_ID)).willReturn(null);

        ResponseEntity<ProductSeoResponse> response = controller.getPublicProductSeo(PRODUCT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getSitemap_usesProvidedBaseUrl() {
        given(seoService.generateSitemap("https://shop.example.com")).willReturn("<urlset/>");

        ResponseEntity<String> response = controller.getSitemap("https://shop.example.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("<urlset/>");
    }

    @Test
    void getSitemap_usesDefaultBaseUrl_whenBlankProvided() {
        given(seoService.generateSitemap("http://localhost:5173")).willReturn("<urlset/>");

        ResponseEntity<String> response = controller.getSitemap("");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getRobots_returns200() {
        given(seoService.generateRobots()).willReturn("User-agent: *\n");

        ResponseEntity<String> response = controller.getRobots();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("User-agent");
    }
}
