package com.shop.cart.controller;

import com.shop.cart.dto.AddToCartRequest;
import com.shop.cart.dto.CartResponse;
import com.shop.cart.dto.UpdateCartItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/** Buyer endpoints for managing the persistent shopping cart (US-CRT-01, US-CRT-02). */
@Tag(name = "Buyer — Cart", description = "Shopping cart management for authenticated buyers")
@RequestMapping("/api/cart")
public interface CartController {

    /**
     * Returns the authenticated buyer's cart, creating an empty one if it does not exist yet (US-CRT-02).
     *
     * @param principal the authenticated buyer principal
     * @return the buyer's cart with HTTP 200
     */
    @Operation(summary = "Get the buyer's cart")
    @ApiResponse(responseCode = "200", description = "Cart returned")
    @GetMapping
    ResponseEntity<CartResponse> getCart(Principal principal);

    /**
     * Adds a product to the cart or increments its quantity if already present (US-CRT-01).
     *
     * @param principal the authenticated buyer principal
     * @param request   the product UUID and quantity to add
     * @return the updated cart with HTTP 200
     */
    @Operation(summary = "Add a product to the cart")
    @ApiResponse(responseCode = "200", description = "Cart updated")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Product not found or not published")
    @ApiResponse(responseCode = "409", description = "Product out of stock or insufficient quantity")
    @PostMapping("/items")
    ResponseEntity<CartResponse> addToCart(Principal principal,
                                           @Valid @RequestBody AddToCartRequest request);

    /**
     * Updates the quantity of an existing cart item (US-CRT-01).
     *
     * @param principal the authenticated buyer principal
     * @param itemId    the cart item UUID
     * @param request   the new quantity
     * @return the updated cart with HTTP 200
     */
    @Operation(summary = "Update cart item quantity")
    @ApiResponse(responseCode = "200", description = "Cart item updated")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    @ApiResponse(responseCode = "409", description = "Requested quantity exceeds available stock")
    @PatchMapping("/items/{itemId}")
    ResponseEntity<CartResponse> updateCartItem(Principal principal,
                                                @PathVariable UUID itemId,
                                                @Valid @RequestBody UpdateCartItemRequest request);

    /**
     * Removes an item from the cart (US-CRT-01).
     *
     * @param principal the authenticated buyer principal
     * @param itemId    the cart item UUID
     * @return the updated cart with HTTP 200
     */
    @Operation(summary = "Remove an item from the cart")
    @ApiResponse(responseCode = "200", description = "Cart item removed")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    @DeleteMapping("/items/{itemId}")
    ResponseEntity<CartResponse> removeCartItem(Principal principal, @PathVariable UUID itemId);
}
