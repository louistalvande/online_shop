package com.shop.catalog.repository;

import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

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

    /**
     * Returns all distinct non-blank product type values across all products,
     * sorted alphabetically (US-CAT-01, US-CAT-02).
     *
     * @return sorted list of distinct product types
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND p.category <> '' ORDER BY p.category")
    List<String> findDistinctTypes();

    /**
     * Returns all distinct non-blank product theme values across all products,
     * sorted alphabetically (US-CAT-01, US-CAT-02).
     *
     * @return sorted list of distinct product themes
     */
    @Query("SELECT DISTINCT p.theme FROM Product p WHERE p.theme IS NOT NULL AND p.theme <> '' ORDER BY p.theme")
    List<String> findDistinctThemes();
}
