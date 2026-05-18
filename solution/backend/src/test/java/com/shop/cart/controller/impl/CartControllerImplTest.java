package com.shop.cart.controller.impl;

import com.shop.cart.dto.CartResponse;
import com.shop.cart.exception.CartItemNotFoundException;
import com.shop.cart.exception.ProductOutOfStockException;
import com.shop.cart.service.CartService;
import com.shop.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unit tests for {@link CartControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class CartControllerImplTest {

    @Mock CartService cartService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final String BUYER_EMAIL = "buyer@example.com";
    private static final UUID ITEM_ID = UUID.randomUUID();

    private final UsernamePasswordAuthenticationToken buyerPrincipal =
            new UsernamePasswordAuthenticationToken(BUYER_EMAIL, null, List.of());

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new CartControllerImpl(cartService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    @Test
    void getCart_returns200() throws Exception {
        given(cartService.getCart(BUYER_EMAIL)).willReturn(emptyCartResponse());

        mvc.perform(get("/api/cart").principal(buyerPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void addToCart_returns200OnSuccess() throws Exception {
        given(cartService.addToCart(eq(BUYER_EMAIL), any())).willReturn(emptyCartResponse());

        mvc.perform(post("/api/cart/items")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + UUID.randomUUID() + "\",\"quantity\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void addToCart_returns409WhenOutOfStock() throws Exception {
        given(cartService.addToCart(eq(BUYER_EMAIL), any()))
                .willThrow(new ProductOutOfStockException(UUID.randomUUID()));
        given(messageSource.getMessage(eq("error.product.out.of.stock"), any(), any(Locale.class)))
                .willReturn("Out of stock");

        mvc.perform(post("/api/cart/items")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + UUID.randomUUID() + "\",\"quantity\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("PRODUCT_OUT_OF_STOCK"));
    }

    @Test
    void updateCartItem_returns200OnSuccess() throws Exception {
        given(cartService.updateCartItem(eq(BUYER_EMAIL), eq(ITEM_ID), any())).willReturn(emptyCartResponse());

        mvc.perform(patch("/api/cart/items/" + ITEM_ID)
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateCartItem_returns404WhenItemNotFound() throws Exception {
        given(cartService.updateCartItem(eq(BUYER_EMAIL), eq(ITEM_ID), any()))
                .willThrow(new CartItemNotFoundException(ITEM_ID));
        given(messageSource.getMessage(eq("error.cart.item.not.found"), any(), any(Locale.class)))
                .willReturn("Cart item not found");

        mvc.perform(patch("/api/cart/items/" + ITEM_ID)
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CART_ITEM_NOT_FOUND"));
    }

    @Test
    void removeCartItem_returns200OnSuccess() throws Exception {
        given(cartService.removeCartItem(eq(BUYER_EMAIL), eq(ITEM_ID))).willReturn(emptyCartResponse());

        mvc.perform(delete("/api/cart/items/" + ITEM_ID).principal(buyerPrincipal))
                .andExpect(status().isOk());
    }

    @Test
    void removeCartItem_returns404WhenItemNotFound() throws Exception {
        given(cartService.removeCartItem(eq(BUYER_EMAIL), eq(ITEM_ID)))
                .willThrow(new CartItemNotFoundException(ITEM_ID));
        given(messageSource.getMessage(eq("error.cart.item.not.found"), any(), any(Locale.class)))
                .willReturn("Cart item not found");

        mvc.perform(delete("/api/cart/items/" + ITEM_ID).principal(buyerPrincipal))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private CartResponse emptyCartResponse() {
        CartResponse r;
        try {
            var ctor = CartResponse.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            r = ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setField(r, "id", UUID.randomUUID());
        setField(r, "buyerId", UUID.randomUUID());
        setField(r, "items", List.of());
        setField(r, "total", BigDecimal.ZERO);
        setField(r, "updatedAt", LocalDateTime.now());
        return r;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            var f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
