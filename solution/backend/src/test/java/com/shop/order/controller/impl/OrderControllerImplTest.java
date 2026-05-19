package com.shop.order.controller.impl;

import com.shop.common.GlobalExceptionHandler;
import com.shop.order.dto.CheckoutInitResponse;
import com.shop.order.dto.OrderResponse;
import com.shop.order.entity.OrderStatus;
import com.shop.order.entity.PaymentMethod;
import com.shop.order.exception.EmptyCartException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.exception.PaymentFailedException;
import com.shop.order.service.OrderService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unit tests for {@link OrderControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class OrderControllerImplTest {

    @Mock OrderService orderService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();

    private final UsernamePasswordAuthenticationToken buyerPrincipal =
            new UsernamePasswordAuthenticationToken(BUYER_ID.toString(), null, List.of());

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new OrderControllerImpl(orderService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    @Test
    void initCheckout_returns201() throws Exception {
        CheckoutInitResponse init = CheckoutInitResponse.forCard(ORDER_ID, "ORD-TEST", new BigDecimal("24.00"), "secret");
        given(orderService.initCheckout(eq(BUYER_ID), any(), any())).willReturn(init);

        mvc.perform(post("/api/orders")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutBody("CARD")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-TEST"))
                .andExpect(jsonPath("$.clientSecret").value("secret"));
    }

    @Test
    void initCheckout_emptyCart_returns400() throws Exception {
        given(orderService.initCheckout(eq(BUYER_ID), any(), any())).willThrow(new EmptyCartException());
        given(messageSource.getMessage(eq("error.order.empty.cart"), any(), any(Locale.class)))
                .willReturn("Empty cart");

        mvc.perform(post("/api/orders")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutBody("CARD")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMPTY_CART"));
    }

    @Test
    void confirmCardPayment_returns200() throws Exception {
        OrderResponse order = buildOrderResponse(OrderStatus.AWAITING_PROCESSING);
        given(orderService.confirmCardPayment(eq(BUYER_ID), eq(ORDER_ID), any())).willReturn(order);

        mvc.perform(post("/api/orders/" + ORDER_ID + "/confirm-payment")
                        .principal(buyerPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-TEST"));
    }

    @Test
    void confirmCardPayment_paymentFailed_returns402() throws Exception {
        given(orderService.confirmCardPayment(eq(BUYER_ID), eq(ORDER_ID), any()))
                .willThrow(new PaymentFailedException("declined"));
        given(messageSource.getMessage(eq("error.order.payment.failed"), any(), any(Locale.class)))
                .willReturn("Payment failed");

        mvc.perform(post("/api/orders/" + ORDER_ID + "/confirm-payment")
                        .principal(buyerPrincipal))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.error").value("PAYMENT_FAILED"));
    }

    @Test
    void getMyOrders_returns200WithList() throws Exception {
        given(orderService.getMyOrders(BUYER_ID)).willReturn(List.of(buildOrderResponse(OrderStatus.AWAITING_PROCESSING)));

        mvc.perform(get("/api/orders").principal(buyerPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-TEST"));
    }

    @Test
    void getMyOrder_returns404WhenNotFound() throws Exception {
        given(orderService.getMyOrder(BUYER_ID, ORDER_ID)).willThrow(new OrderNotFoundException(ORDER_ID));
        given(messageSource.getMessage(eq("error.order.not.found"), any(), any(Locale.class)))
                .willReturn("Order not found");

        mvc.perform(get("/api/orders/" + ORDER_ID).principal(buyerPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String checkoutBody(String paymentMethod) {
        return """
                {
                  "deliveryAddressLine": "1 rue Test",
                  "deliveryCity": "Paris",
                  "deliveryPostalCode": "75001",
                  "deliveryCountryCode": "FR",
                  "carrierId": "%s",
                  "paymentMethod": "%s"
                }""".formatted(UUID.randomUUID(), paymentMethod);
    }

    private OrderResponse buildOrderResponse(OrderStatus status) {
        OrderResponse r;
        try {
            var ctor = OrderResponse.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            r = ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setField(r, "id", ORDER_ID);
        setField(r, "orderNumber", "ORD-TEST");
        setField(r, "buyerId", BUYER_ID);
        setField(r, "carrierId", UUID.randomUUID());
        setField(r, "carrierName", "Test Carrier");
        setField(r, "carrierTrackingUrl", "https://track.example.com");
        setField(r, "deliveryAddressLine", "1 rue Test");
        setField(r, "deliveryCity", "Paris");
        setField(r, "deliveryPostalCode", "75001");
        setField(r, "deliveryCountryCode", "FR");
        setField(r, "paymentMethod", PaymentMethod.CARD);
        setField(r, "status", status);
        setField(r, "totalAmountTtc", new BigDecimal("24.00"));
        setField(r, "lines", new ArrayList<>());
        setField(r, "createdAt", LocalDateTime.now());
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
