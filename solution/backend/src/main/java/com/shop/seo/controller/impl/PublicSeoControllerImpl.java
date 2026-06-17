package com.shop.seo.controller.impl;

import com.shop.seo.controller.PublicSeoController;
import com.shop.seo.dto.ProductSeoResponse;
import com.shop.seo.dto.ShopSeoResponse;
import com.shop.seo.service.SeoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** {@link PublicSeoController} implementation. */
@RestController
public class PublicSeoControllerImpl implements PublicSeoController {

    private final SeoService seoService;

    @Value("${shop.public.base-url:http://localhost:5173}")
    private String defaultBaseUrl;

    /**
     * Constructs the controller with the SEO service.
     *
     * @param seoService the SEO business service
     */
    public PublicSeoControllerImpl(SeoService seoService) {
        this.seoService = seoService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ShopSeoResponse> getPublicShopSeo() {
        return ResponseEntity.ok(seoService.getPublicShopSeo());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductSeoResponse> getPublicProductSeo(UUID productId) {
        ProductSeoResponse override = seoService.getPublicProductSeo(productId);
        if (override == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(override);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<String> getSitemap(String baseUrl) {
        String effectiveBase = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : defaultBaseUrl;
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(seoService.generateSitemap(effectiveBase));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<String> getRobots() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(seoService.generateRobots());
    }
}
