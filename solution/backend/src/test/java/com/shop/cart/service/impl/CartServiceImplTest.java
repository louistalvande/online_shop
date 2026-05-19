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
import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link CartServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ProductRepository productRepository;
    @Mock AccountRepository accountRepository;

    CartServiceImpl service;

    private static final String BUYER_EMAIL = "buyer@example.com";
    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID CART_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CartServiceImpl(cartRepository, cartItemRepository, productRepository, accountRepository);
    }

    // -------------------------------------------------------------------------
    // getCart
    // -------------------------------------------------------------------------

    @Test
    void getCart_returnsExistingCart() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount()));
        Cart cart = buildCart();
        given(cartRepository.findByBuyerId(BUYER_ID)).willReturn(Optional.of(cart));

        CartResponse response = service.getCart(BUYER_EMAIL);

        assertThat(response.getBuyerId()).isEqualTo(BUYER_ID);
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void getCart_returnsEmptyCartWhenNoneExists() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount()));
        given(cartRepository.findByBuyerId(BUYER_ID)).willReturn(Optional.empty());

        CartResponse response = service.getCart(BUYER_EMAIL);

        assertThat(response.getBuyerId()).isEqualTo(BUYER_ID);
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void getCart_throwsWhenBuyerNotFound() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCart(BUYER_EMAIL))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // addToCart
    // -------------------------------------------------------------------------

    @Test
    void addToCart_createsNewItemWhenProductNotInCart() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount()));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(publishedProductWithStock(10)));
        Cart cart = buildCart();
        given(cartRepository.findByBuyerId(BUYER_ID))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(cart));
        given(cartRepository.save(any())).willReturn(cart);
        given(cartItemRepository.findByCart_IdAndProduct_Id(any(), any())).willReturn(Optional.empty());

        AddToCartRequest req = new AddToCartRequest();
        req.setProductId(PRODUCT_ID);
        req.setQuantity(2);

        service.addToCart(BUYER_EMAIL, req);

        then(cartItemRepository).should().save(any(CartItem.class));
    }

    @Test
    void addToCart_throwsWhenProductIsOutOfStock() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount()));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(publishedProductWithStock(0)));

        AddToCartRequest req = new AddToCartRequest();
        req.setProductId(PRODUCT_ID);
        req.setQuantity(1);

        assertThatThrownBy(() -> service.addToCart(BUYER_EMAIL, req))
                .isInstanceOf(ProductOutOfStockException.class);
        then(cartItemRepository).shouldHaveNoInteractions();
    }

    @Test
    void addToCart_throwsWhenProductNotPublished() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount()));
        Product archived = publishedProductWithStock(5);
        archived.setStatus(ProductStatus.ARCHIVED);
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(archived));

        AddToCartRequest req = new AddToCartRequest();
        req.setProductId(PRODUCT_ID);
        req.setQuantity(1);

        assertThatThrownBy(() -> service.addToCart(BUYER_EMAIL, req))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void addToCart_throwsWhenRequestedQuantityExceedsStock() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount()));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(publishedProductWithStock(3)));

        AddToCartRequest req = new AddToCartRequest();
        req.setProductId(PRODUCT_ID);
        req.setQuantity(5);

        assertThatThrownBy(() -> service.addToCart(BUYER_EMAIL, req))
                .isInstanceOf(ProductOutOfStockException.class);
    }

    // -------------------------------------------------------------------------
    // updateCartItem
    // -------------------------------------------------------------------------

    @Test
    void updateCartItem_throwsWhenItemNotOwnedByBuyer() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount()));
        given(cartItemRepository.findById(ITEM_ID)).willReturn(Optional.empty());

        UpdateCartItemRequest req = new UpdateCartItemRequest();
        req.setQuantity(2);

        assertThatThrownBy(() -> service.updateCartItem(BUYER_EMAIL, ITEM_ID, req))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // removeCartItem
    // -------------------------------------------------------------------------

    @Test
    void removeCartItem_deletesItemAndTouchesCart() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount()));
        Cart cart = buildCart();
        CartItem item = buildItemInCart(cart);
        cart.getItems().add(item);
        given(cartItemRepository.findById(ITEM_ID)).willReturn(Optional.of(item));
        given(cartRepository.findById(cart.getId())).willReturn(Optional.of(cart));
        given(cartRepository.save(any())).willReturn(cart);

        service.removeCartItem(BUYER_EMAIL, ITEM_ID);

        then(cartItemRepository).should().delete(item);
    }

    @Test
    void removeCartItem_throwsWhenItemNotFound() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount()));
        given(cartItemRepository.findById(ITEM_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeCartItem(BUYER_EMAIL, ITEM_ID))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Account buyerAccount() {
        Account a = new Account();
        try {
            var f = Account.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(a, BUYER_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        a.setEmail(BUYER_EMAIL);
        return a;
    }

    private Product publishedProductWithStock(int stock) {
        Product p = new Product();
        try {
            var f = Product.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(p, PRODUCT_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        p.setName("Test product");
        p.setPriceExclTax(new BigDecimal("20.00"));
        p.setQuantity(stock);
        p.setStatus(ProductStatus.PUBLISHED);
        return p;
    }

    private Cart buildCart() {
        Cart c = new Cart();
        try {
            var f = Cart.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(c, CART_ID);
            var items = Cart.class.getDeclaredField("items");
            items.setAccessible(true);
            items.set(c, new ArrayList<>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        c.setBuyerId(BUYER_ID);
        return c;
    }

    private CartItem buildItemInCart(Cart cart) {
        CartItem item = new CartItem();
        try {
            var f = CartItem.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(item, ITEM_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        item.setCart(cart);
        item.setProduct(publishedProductWithStock(10));
        item.setQuantity(2);
        return item;
    }
}
