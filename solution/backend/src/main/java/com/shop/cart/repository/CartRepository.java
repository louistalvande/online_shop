package com.shop.cart.repository;

import com.shop.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** JPA repository for {@link Cart} entities. */
public interface CartRepository extends JpaRepository<Cart, UUID> {

    /**
     * Finds the cart belonging to a given buyer.
     *
     * @param buyerId the buyer account UUID
     * @return the buyer's cart, or empty if no cart exists yet
     */
    Optional<Cart> findByBuyerId(UUID buyerId);
}
