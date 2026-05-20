package com.shop.cart.service;

import com.shop.cart.dto.AddToCartRequest;
import com.shop.cart.dto.CartResponse;
import com.shop.cart.dto.UpdateCartItemRequest;
import com.shop.cart.exception.CartItemNotFoundException;
import com.shop.cart.exception.ProductOutOfStockException;
import com.shop.catalog.exception.ProductNotFoundException;

import java.util.UUID;

/** Business operations for managing a buyer's persistent cart (US-CRT-01, US-CRT-02). */
public interface CartService {

    /**
     * Returns the current cart for the authenticated buyer.
     * Creates an empty cart if none exists yet.
     *
     * @param buyerEmail the email address of the authenticated buyer
     * @return the buyer's cart (never null)
     */
    CartResponse getCart(String buyerEmail);

    /**
     * Adds a product to the buyer's cart.
     * If the product is already in the cart the quantities are summed.
     * Throws if the product is out of stock or does not exist.
     *
     * @param buyerEmail the email address of the authenticated buyer
     * @param request    the product to add and the quantity
     * @return the updated cart
     * @throws ProductNotFoundException  if the product UUID is unknown or not published
     * @throws ProductOutOfStockException if the requested quantity exceeds available stock
     */
    CartResponse addToCart(String buyerEmail, AddToCartRequest request);

    /**
     * Updates the quantity of an existing cart item.
     *
     * @param buyerEmail the email address of the authenticated buyer
     * @param itemId     the cart item UUID
     * @param request    the new quantity
     * @return the updated cart
     * @throws CartItemNotFoundException  if the item UUID does not exist or does not belong to the buyer
     * @throws ProductOutOfStockException if the new quantity exceeds available stock
     */
    CartResponse updateCartItem(String buyerEmail, UUID itemId, UpdateCartItemRequest request);

    /**
     * Removes an item from the buyer's cart.
     *
     * @param buyerEmail the email address of the authenticated buyer
     * @param itemId     the cart item UUID
     * @return the updated cart
     * @throws CartItemNotFoundException if the item UUID does not exist or does not belong to the buyer
     */
    CartResponse removeCartItem(String buyerEmail, UUID itemId);
}
