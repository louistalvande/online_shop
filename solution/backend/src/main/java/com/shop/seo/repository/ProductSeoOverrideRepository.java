package com.shop.seo.repository;

import com.shop.seo.entity.ProductSeoOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Data access for per-product SEO override records. */
public interface ProductSeoOverrideRepository extends JpaRepository<ProductSeoOverride, UUID> {
}
