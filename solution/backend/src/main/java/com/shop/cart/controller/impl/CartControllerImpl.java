package com.shop.cart.controller.impl;

import com.shop.cart.controller.CartController;
import com.shop.cart.dto.AddToCartRequest;
import com.shop.cart.dto.CartResponse;
import com.shop.cart.dto.UpdateCartItemRequest;
import com.shop.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

/** {@link CartController} implementation. */
@RestController
public class CartControllerImpl implements CartController {

    private final CartService cartService;

    /**
     * Constructs the controller with its required service.
     *
     * @param cartService the shopping cart service
     */
    public CartControllerImpl(CartService cartService) {
        this.cartService = cartService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CartResponse> addToCart(Principal principal, AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(principal.getName(), request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CartResponse> updateCartItem(Principal principal,
                                                       UUID itemId,
                                                       UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(principal.getName(), itemId, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CartResponse> removeCartItem(Principal principal, UUID itemId) {
        return ResponseEntity.ok(cartService.removeCartItem(principal.getName(), itemId));
    }
}
