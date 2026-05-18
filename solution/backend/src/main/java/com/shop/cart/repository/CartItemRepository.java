package com.shop.cart.repository;

import com.shop.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** JPA repository for {@link CartItem} entities. */
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Finds a cart item by its cart and product, used to detect duplicates before adding.
     *
     * @param cartId    the cart UUID
     * @param productId the product UUID
     * @return the existing item, or empty if the product is not yet in the cart
     */
    Optional<CartItem> findByCart_IdAndProduct_Id(UUID cartId, UUID productId);
}
