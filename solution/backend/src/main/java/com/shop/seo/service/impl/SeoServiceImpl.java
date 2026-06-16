package com.shop.seo.service.impl;

import com.shop.catalog.entity.Product;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.repository.ProductRepository;
import com.shop.seo.dto.ProductSeoRequest;
import com.shop.seo.dto.ProductSeoResponse;
import com.shop.seo.dto.ShopSeoRequest;
import com.shop.seo.dto.ShopSeoResponse;
import com.shop.seo.entity.ProductSeoOverride;
import com.shop.seo.entity.ShopSeo;
import com.shop.seo.exception.SeoNotFoundException;
import com.shop.seo.repository.ProductSeoOverrideRepository;
import com.shop.seo.repository.ShopSeoRepository;
import com.shop.seo.service.SeoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** {@link SeoService} implementation. */
@Service
@Transactional
public class SeoServiceImpl implements SeoService {

    private final ShopSeoRepository shopSeoRepository;
    private final ProductSeoOverrideRepository productSeoOverrideRepository;
    private final ProductRepository productRepository;

    /**
     * Constructs the service with required repositories.
     *
     * @param shopSeoRepository            repository for shop-wide SEO config
     * @param productSeoOverrideRepository repository for per-product overrides
     * @param productRepository            repository for product lookups
     */
    public SeoServiceImpl(ShopSeoRepository shopSeoRepository,
                          ProductSeoOverrideRepository productSeoOverrideRepository,
                          ProductRepository productRepository) {
        this.shopSeoRepository = shopSeoRepository;
        this.productSeoOverrideRepository = productSeoOverrideRepository;
        this.productRepository = productRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ShopSeoResponse getShopSeo() {
        return shopSeoRepository.findAll().stream()
                .findFirst()
                .map(ShopSeoResponse::from)
                .orElseGet(() -> ShopSeoResponse.from(new ShopSeo()));
    }

    /** {@inheritDoc} */
    @Override
    public ShopSeoResponse saveShopSeo(ShopSeoRequest request) {
        List<ShopSeo> existing = shopSeoRepository.findAll();
        ShopSeo entity = existing.isEmpty() ? new ShopSeo() : existing.get(0);
        applyShopSeoRequest(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        return ShopSeoResponse.from(shopSeoRepository.save(entity));
    }

    /** {@inheritDoc} */
    @Override
    public ProductSeoResponse saveProductSeo(UUID productId, ProductSeoRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        ProductSeoOverride override = productSeoOverrideRepository.findById(productId)
                .orElseGet(() -> {
                    ProductSeoOverride o = new ProductSeoOverride();
                    o.setProduct(product);
                    return o;
                });
        override.setSeoTitle(request.getSeoTitle());
        override.setSeoDescription(request.getSeoDescription());
        override.setSeoKeywords(request.getSeoKeywords());
        override.setOgImageUrl(request.getOgImageUrl());
        override.setUpdatedAt(LocalDateTime.now());
        return ProductSeoResponse.from(productSeoOverrideRepository.save(override));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ProductSeoResponse getProductSeo(UUID productId) {
        return productSeoOverrideRepository.findById(productId)
                .map(ProductSeoResponse::from)
                .orElseThrow(() -> new SeoNotFoundException(productId));
    }

    /** {@inheritDoc} */
    @Override
    public void deleteProductSeo(UUID productId) {
        ProductSeoOverride override = productSeoOverrideRepository.findById(productId)
                .orElseThrow(() -> new SeoNotFoundException(productId));
        productSeoOverrideRepository.delete(override);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ShopSeoResponse getPublicShopSeo() {
        return getShopSeo();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ProductSeoResponse getPublicProductSeo(UUID productId) {
        return productSeoOverrideRepository.findById(productId)
                .map(ProductSeoResponse::from)
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public String generateSitemap(String baseUrl) {
        ShopSeoResponse seo = getShopSeo();
        String freq = seo.getSitemapChangefreq() != null ? seo.getSitemapChangefreq() : "weekly";
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        sb.append("  <url><loc>").append(baseUrl).append("/</loc>")
          .append("<changefreq>").append(freq).append("</changefreq></url>\n");
        if (seo.isIndexCatalog()) {
            sb.append("  <url><loc>").append(baseUrl).append("/catalog</loc>")
              .append("<changefreq>").append(freq).append("</changefreq></url>\n");
        }
        if (seo.isIndexProducts()) {
            productRepository.findAll().forEach(p ->
                sb.append("  <url><loc>").append(baseUrl).append("/catalog/").append(p.getId())
                  .append("</loc><changefreq>").append(freq).append("</changefreq></url>\n"));
        }
        sb.append("</urlset>");
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public String generateRobots() {
        ShopSeoResponse seo = getShopSeo();
        StringBuilder sb = new StringBuilder();
        sb.append("User-agent: *\n");
        if (!seo.isIndexProducts()) {
            sb.append("Disallow: /catalog/\n");
        }
        if (!seo.isIndexCatalog()) {
            sb.append("Disallow: /catalog\n");
        }
        String disallowPaths = seo.getRobotsDisallowPaths();
        if (disallowPaths != null && !disallowPaths.isBlank()) {
            for (String path : disallowPaths.split("\\r?\\n")) {
                String trimmed = path.trim();
                if (!trimmed.isEmpty()) {
                    sb.append("Disallow: ").append(trimmed).append("\n");
                }
            }
        }
        sb.append("Sitemap: /sitemap.xml\n");
        return sb.toString();
    }

    private void applyShopSeoRequest(ShopSeo entity, ShopSeoRequest request) {
        entity.setSeoTitle(request.getSeoTitle());
        entity.setSeoDescription(request.getSeoDescription());
        entity.setSeoKeywords(request.getSeoKeywords());
        entity.setOgImageUrl(request.getOgImageUrl());
        entity.setCanonicalUrl(request.getCanonicalUrl());
        entity.setRobotsDisallowPaths(request.getRobotsDisallowPaths());
        if (request.getSitemapChangefreq() != null) {
            entity.setSitemapChangefreq(request.getSitemapChangefreq());
        }
        if (request.getIndexProducts() != null) {
            entity.setIndexProducts(request.getIndexProducts());
        }
        if (request.getIndexCatalog() != null) {
            entity.setIndexCatalog(request.getIndexCatalog());
        }
        entity.setGoogleVerification(request.getGoogleVerification());
        entity.setGa4Id(request.getGa4Id());
        entity.setBingVerification(request.getBingVerification());
    }
}
