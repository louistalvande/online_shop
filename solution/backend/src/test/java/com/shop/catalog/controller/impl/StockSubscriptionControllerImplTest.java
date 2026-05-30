package com.shop.catalog.controller.impl;

import com.shop.catalog.dto.StockSubscriptionResponse;
import com.shop.catalog.exception.AlreadySubscribedException;
import com.shop.catalog.exception.ProductInStockException;
import com.shop.catalog.exception.StockSubscriptionNotFoundException;
import com.shop.catalog.service.StockSubscriptionService;
import com.shop.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Unit tests for {@link StockSubscriptionControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class StockSubscriptionControllerImplTest {

    @Mock StockSubscriptionService subscriptionService;
    @Mock MessageSource messageSource;

    MockMvc mvc;
    Principal buyerPrincipal = () -> "buyer@example.com";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new StockSubscriptionControllerImpl(subscriptionService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    private StockSubscriptionResponse buildResponse() {
        StockSubscriptionResponse r = new StockSubscriptionResponse() {
        };
        return r;
    }

    private static StockSubscriptionResponse sampleResponse(UUID productId) {
        // Use a plain object since fields are private — we test HTTP status, not body content
        return new StockSubscriptionResponse();
    }

    /** POST / returns 201 when subscription is created. */
    @Test
    void subscribe_returns201_whenCreated() throws Exception {
        UUID productId = UUID.randomUUID();
        given(subscriptionService.subscribe(eq("buyer@example.com"), eq(productId)))
                .willReturn(sampleResponse(productId));

        mvc.perform(post("/api/profile/stock-subscriptions")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\"}"))
                .andExpect(status().isCreated());

        then(subscriptionService).should().subscribe("buyer@example.com", productId);
    }

    /** POST / returns 409 when the buyer is already subscribed. */
    @Test
    void subscribe_returns409_whenAlreadySubscribed() throws Exception {
        UUID productId = UUID.randomUUID();
        willThrow(new AlreadySubscribedException(productId))
                .given(subscriptionService).subscribe(any(), eq(productId));
        given(messageSource.getMessage(eq("error.subscription.already.exists"), any(), any(Locale.class)))
                .willReturn("Already subscribed");

        mvc.perform(post("/api/profile/stock-subscriptions")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ALREADY_SUBSCRIBED"));
    }

    /** POST / returns 409 when the product is in stock. */
    @Test
    void subscribe_returns409_whenProductInStock() throws Exception {
        UUID productId = UUID.randomUUID();
        willThrow(new ProductInStockException(productId))
                .given(subscriptionService).subscribe(any(), eq(productId));
        given(messageSource.getMessage(eq("error.subscription.product.in.stock"), any(), any(Locale.class)))
                .willReturn("Product in stock");

        mvc.perform(post("/api/profile/stock-subscriptions")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("PRODUCT_IN_STOCK"));
    }

    /** DELETE /{productId} returns 204 when subscription is deleted. */
    @Test
    void unsubscribe_returns204_whenFound() throws Exception {
        UUID productId = UUID.randomUUID();

        mvc.perform(delete("/api/profile/stock-subscriptions/{productId}", productId)
                        .principal(buyerPrincipal))
                .andExpect(status().isNoContent());

        then(subscriptionService).should().unsubscribe("buyer@example.com", productId);
    }

    /** DELETE /{productId} returns 404 when no subscription exists. */
    @Test
    void unsubscribe_returns404_whenNotFound() throws Exception {
        UUID productId = UUID.randomUUID();
        willThrow(new StockSubscriptionNotFoundException(productId))
                .given(subscriptionService).unsubscribe(any(), eq(productId));
        given(messageSource.getMessage(eq("error.subscription.not.found"), any(), any(Locale.class)))
                .willReturn("Subscription not found");

        mvc.perform(delete("/api/profile/stock-subscriptions/{productId}", productId)
                        .principal(buyerPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("SUBSCRIPTION_NOT_FOUND"));
    }

    /** GET / returns 200 with the list of active subscriptions. */
    @Test
    void listSubscriptions_returns200() throws Exception {
        given(subscriptionService.listSubscriptions("buyer@example.com")).willReturn(List.of());

        mvc.perform(get("/api/profile/stock-subscriptions").principal(buyerPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
