package com.shop.seo.repository;

import com.shop.seo.entity.ShopSeo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Data access for the singleton {@link ShopSeo} configuration record. */
public interface ShopSeoRepository extends JpaRepository<ShopSeo, UUID> {
}
