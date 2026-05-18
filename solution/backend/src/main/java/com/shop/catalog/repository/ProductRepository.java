package com.shop.catalog.repository;

import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Data access layer for {@link Product} entities. */
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    /**
     * Returns all products belonging to a vendor, ordered by creation date descending.
     *
     * @param vendorId the vendor account UUID
     * @return list of the vendor's products
     */
    List<Product> findByVendorIdOrderByCreatedAtDesc(UUID vendorId);

    /**
     * Returns all published products for buyer catalog browsing (US-SHP-01).
     *
     * @return list of published products
     */
    List<Product> findByStatus(ProductStatus status);

    /**
     * Finds a product by its ID, ensuring it belongs to the given vendor.
     *
     * @param id       the product UUID
     * @param vendorId the vendor account UUID
     * @return the matching product, or empty if not found or not owned by the vendor
     */
    Optional<Product> findByIdAndVendorId(UUID id, UUID vendorId);
}
