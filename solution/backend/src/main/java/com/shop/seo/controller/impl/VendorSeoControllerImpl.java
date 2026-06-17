package com.shop.seo.controller.impl;

import com.shop.seo.controller.VendorSeoController;
import com.shop.seo.dto.ProductSeoRequest;
import com.shop.seo.dto.ProductSeoResponse;
import com.shop.seo.dto.ShopSeoRequest;
import com.shop.seo.dto.ShopSeoResponse;
import com.shop.seo.service.SeoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** {@link VendorSeoController} implementation. */
@RestController
public class VendorSeoControllerImpl implements VendorSeoController {

    private final SeoService seoService;

    /**
     * Constructs the controller with the SEO service.
     *
     * @param seoService the SEO business service
     */
    public VendorSeoControllerImpl(SeoService seoService) {
        this.seoService = seoService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ShopSeoResponse> getShopSeo() {
        return ResponseEntity.ok(seoService.getShopSeo());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ShopSeoResponse> saveShopSeo(ShopSeoRequest request) {
        return ResponseEntity.ok(seoService.saveShopSeo(request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductSeoResponse> getProductSeo(UUID productId) {
        return ResponseEntity.ok(seoService.getProductSeo(productId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProductSeoResponse> saveProductSeo(UUID productId, ProductSeoRequest request) {
        return ResponseEntity.ok(seoService.saveProductSeo(productId, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> deleteProductSeo(UUID productId) {
        seoService.deleteProductSeo(productId);
        return ResponseEntity.noContent().build();
    }
}
