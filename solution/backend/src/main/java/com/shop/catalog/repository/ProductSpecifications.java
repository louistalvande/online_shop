package com.shop.catalog.repository;

import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Factory of {@link Specification} predicates for buyer-facing catalog filtering. */
public final class ProductSpecifications {

    private static final BigDecimal VAT_RATE = new BigDecimal("1.20");

    private ProductSpecifications() {}

    /**
     * Restricts to published products only.
     *
     * @return the specification
     */
    public static Specification<Product> published() {
        return (root, query, cb) -> cb.equal(root.get("status"), ProductStatus.PUBLISHED);
    }

    /**
     * Restricts to products whose category matches exactly (case-insensitive).
     * Returns an unrestricted predicate when {@code category} is null or blank.
     *
     * @param category the category string to match
     * @return the specification
     */
    public static Specification<Product> withCategory(String category) {
        return (root, query, cb) -> {
            if (category == null || category.isBlank()) return null;
            return cb.equal(cb.lower(root.get("category")), category.toLowerCase());
        };
    }

    /**
     * Restricts to products whose TTC price (priceExclTax × 1.20) is at most {@code maxPriceTTC}.
     * Returns an unrestricted predicate when {@code maxPriceTTC} is null.
     *
     * @param maxPriceTTC the maximum all-inclusive price in euros
     * @return the specification
     */
    public static Specification<Product> withMaxPriceTTC(BigDecimal maxPriceTTC) {
        return (root, query, cb) -> {
            if (maxPriceTTC == null) return null;
            BigDecimal maxPriceExclTax = maxPriceTTC.divide(VAT_RATE, 4, RoundingMode.HALF_UP);
            return cb.lessThanOrEqualTo(root.get("priceExclTax"), maxPriceExclTax);
        };
    }

    /**
     * Restricts to products with at least one unit in stock when {@code inStockOnly} is true.
     * Returns an unrestricted predicate when {@code inStockOnly} is false.
     *
     * @param inStockOnly whether to exclude out-of-stock products
     * @return the specification
     */
    public static Specification<Product> inStockOnly(boolean inStockOnly) {
        return (root, query, cb) -> {
            if (!inStockOnly) return null;
            return cb.greaterThan(root.get("quantity"), 0);
        };
    }

    /**
     * Restricts to products whose theme matches exactly (case-insensitive).
     * Returns an unrestricted predicate when {@code theme} is null or blank.
     *
     * @param theme the theme string to match
     * @return the specification
     */
    public static Specification<Product> withTheme(String theme) {
        return (root, query, cb) -> {
            if (theme == null || theme.isBlank()) return null;
            return cb.equal(cb.lower(root.get("theme")), theme.toLowerCase());
        };
    }

    /**
     * Restricts to products whose name contains the given search term (case-insensitive).
     * Returns an unrestricted predicate when {@code search} is null or blank.
     *
     * @param search the text to search for within product names
     * @return the specification
     */
    public static Specification<Product> nameLike(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }
}
