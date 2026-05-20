package com.shop.cart.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.repository.AccountRepository;
import com.shop.cart.dto.AddToCartRequest;
import com.shop.cart.dto.CartResponse;
import com.shop.cart.dto.UpdateCartItemRequest;
import com.shop.cart.entity.Cart;
import com.shop.cart.entity.CartItem;
import com.shop.cart.exception.CartItemNotFoundException;
import com.shop.cart.exception.ProductOutOfStockException;
import com.shop.cart.repository.CartItemRepository;
import com.shop.cart.repository.CartRepository;
import com.shop.cart.service.CartService;
import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/** {@link CartService} implementation. */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    /**
     * Constructs the service with its required repositories.
     *
     * @param cartRepository     the cart JPA repository
     * @param cartItemRepository the cart item JPA repository
     * @param productRepository  the product JPA repository
     * @param accountRepository  the account JPA repository
     */
    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository,
                           AccountRepository accountRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String buyerEmail) {
        UUID buyerId = resolveBuyerId(buyerEmail);
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseGet(() -> buildEmptyCart(buyerId));
        return CartResponse.from(cart);
    }

    /** {@inheritDoc} */
    @Override
    public CartResponse addToCart(String buyerEmail, AddToCartRequest request) {
        UUID buyerId = resolveBuyerId(buyerEmail);
        Product product = resolvePublishedProduct(request.getProductId());
        validateStock(product, request.getQuantity());

        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setBuyerId(buyerId);
                    return cartRepository.save(c);
                });

        CartItem item = cartItemRepository
                .findByCart_IdAndProduct_Id(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        int newQty = item.getQuantity() + request.getQuantity();
        validateStock(product, newQty);
        item.setQuantity(newQty);
        cartItemRepository.save(item);

        touchCart(cart);
        return CartResponse.from(cartRepository.findByBuyerId(buyerId).orElseThrow());
    }

    /** {@inheritDoc} */
    @Override
    public CartResponse updateCartItem(String buyerEmail, UUID itemId, UpdateCartItemRequest request) {
        UUID buyerId = resolveBuyerId(buyerEmail);
        CartItem item = resolveOwnedItem(buyerId, itemId);
        validateStock(item.getProduct(), request.getQuantity());

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        Cart cart = item.getCart();
        touchCart(cart);
        return CartResponse.from(cartRepository.findById(cart.getId()).orElseThrow());
    }

    /** {@inheritDoc} */
    @Override
    public CartResponse removeCartItem(String buyerEmail, UUID itemId) {
        UUID buyerId = resolveBuyerId(buyerEmail);
        CartItem item = resolveOwnedItem(buyerId, itemId);
        Cart cart = item.getCart();

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        touchCart(cart);
        return CartResponse.from(cartRepository.findById(cart.getId()).orElseThrow());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UUID resolveBuyerId(String email) {
        return accountRepository.findByEmail(email)
                .map(Account::getId)
                .orElseThrow(() -> new AccountNotFoundException(email));
    }

    private Product resolvePublishedProduct(UUID productId) {
        return productRepository.findById(productId)
                .filter(p -> p.getStatus() == ProductStatus.PUBLISHED)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    /**
     * Validates that the requested quantity does not exceed the product's available stock.
     * Also covers the "out of stock" case (stock = 0).
     *
     * @param product  the product to check
     * @param quantity the desired quantity
     * @throws ProductOutOfStockException if stock is insufficient
     */
    private void validateStock(Product product, int quantity) {
        if (product.getQuantity() < quantity) {
            throw new ProductOutOfStockException(product.getId());
        }
    }

    private CartItem resolveOwnedItem(UUID buyerId, UUID itemId) {
        return cartItemRepository.findById(itemId)
                .filter(i -> i.getCart().getBuyerId().equals(buyerId))
                .orElseThrow(() -> new CartItemNotFoundException(itemId));
    }

    /** Returns a transient empty cart (not persisted) for read-only getCart calls. */
    private Cart buildEmptyCart(UUID buyerId) {
        Cart cart = new Cart();
        cart.setBuyerId(buyerId);
        return cart;
    }

    private void touchCart(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }
}
