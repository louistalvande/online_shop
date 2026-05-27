package com.shop.catalog.repository;

import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

/** Data access layer for {@link Product} entities. */
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    /**
     * Returns all products ordered by creation date descending (vendor back-office listing).
     *
     * @return list of all products
     */
    List<Product> findAllByOrderByCreatedAtDesc();

    /**
     * Returns all published products for buyer catalog browsing (US-SHP-01).
     *
     * @return list of published products
     */
    List<Product> findByStatus(ProductStatus status);
}
