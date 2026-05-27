package com.shop.order.controller.impl;

import com.shop.common.GlobalExceptionHandler;
import com.shop.order.dto.CheckoutInitResponse;
import com.shop.order.dto.OrderResponse;
import com.shop.order.entity.OrderStatus;
import com.shop.order.entity.PaymentMethod;
import com.shop.order.exception.EmptyCartException;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.MissingBuyerIbanException;
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

    private static final String BUYER_EMAIL = "buyer@test.com";
    private static final UUID ORDER_ID = UUID.randomUUID();

    private final UsernamePasswordAuthenticationToken buyerPrincipal =
            new UsernamePasswordAuthenticationToken(BUYER_EMAIL, null, List.of());

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
        given(orderService.initCheckout(eq(BUYER_EMAIL), any(), any())).willReturn(init);

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
        given(orderService.initCheckout(eq(BUYER_EMAIL), any(), any())).willThrow(new EmptyCartException());
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
        given(orderService.confirmCardPayment(eq(BUYER_EMAIL), eq(ORDER_ID), any())).willReturn(order);

        mvc.perform(post("/api/orders/" + ORDER_ID + "/confirm-payment")
                        .principal(buyerPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-TEST"));
    }

    @Test
    void confirmCardPayment_paymentFailed_returns402() throws Exception {
        given(orderService.confirmCardPayment(eq(BUYER_EMAIL), eq(ORDER_ID), any()))
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
        given(orderService.getMyOrders(BUYER_EMAIL)).willReturn(List.of(buildOrderResponse(OrderStatus.AWAITING_PROCESSING)));

        mvc.perform(get("/api/orders").principal(buyerPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-TEST"));
    }

    @Test
    void getMyOrder_returns404WhenNotFound() throws Exception {
        given(orderService.getMyOrder(BUYER_EMAIL, ORDER_ID)).willThrow(new OrderNotFoundException(ORDER_ID));
        given(messageSource.getMessage(eq("error.order.not.found"), any(), any(Locale.class)))
                .willReturn("Order not found");

        mvc.perform(get("/api/orders/" + ORDER_ID).principal(buyerPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
    }

    @Test
    void cancelOrder_card_returns200() throws Exception {
        OrderResponse cancelled = buildOrderResponse(OrderStatus.CANCELLED);
        given(orderService.cancelOrder(eq(BUYER_EMAIL), eq(ORDER_ID), any(), any())).willReturn(cancelled);

        mvc.perform(post("/api/orders/" + ORDER_ID + "/cancel")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_missingIban_returns422() throws Exception {
        given(orderService.cancelOrder(eq(BUYER_EMAIL), eq(ORDER_ID), any(), any()))
                .willThrow(new MissingBuyerIbanException(ORDER_ID));
        given(messageSource.getMessage(eq("error.order.missing.buyer.iban"), any(), any(Locale.class)))
                .willReturn("IBAN required");

        mvc.perform(post("/api/orders/" + ORDER_ID + "/cancel")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("MISSING_BUYER_IBAN"));
    }

    @Test
    void cancelOrder_invalidState_returns409() throws Exception {
        given(orderService.cancelOrder(eq(BUYER_EMAIL), eq(ORDER_ID), any(), any()))
                .willThrow(new InvalidOrderStateException(ORDER_ID, OrderStatus.SHIPPED));
        given(messageSource.getMessage(eq("error.order.invalid.state"), any(), any(Locale.class)))
                .willReturn("Invalid state");

        mvc.perform(post("/api/orders/" + ORDER_ID + "/cancel")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_ORDER_STATE"));
    }

    // ─── requestPostShipmentCancellation ─────────────────────────────────────

    @Test
    void requestPostShipmentCancellation_returns200() throws Exception {
        OrderResponse response = buildOrderResponse(OrderStatus.CANCELLATION_REQUESTED_AFTER_SHIPMENT);
        given(orderService.requestPostShipmentCancellation(eq(BUYER_EMAIL), eq(ORDER_ID), any(), any(), any()))
                .willReturn(response);

        mvc.perform(post("/api/orders/" + ORDER_ID + "/request-post-shipment-cancellation")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Product damaged\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLATION_REQUESTED_AFTER_SHIPMENT"));
    }

    @Test
    void requestPostShipmentCancellation_missingReason_returns400() throws Exception {
        mvc.perform(post("/api/orders/" + ORDER_ID + "/request-post-shipment-cancellation")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestPostShipmentCancellation_invalidState_returns409() throws Exception {
        given(orderService.requestPostShipmentCancellation(eq(BUYER_EMAIL), eq(ORDER_ID), any(), any(), any()))
                .willThrow(new InvalidOrderStateException(ORDER_ID, OrderStatus.AWAITING_PROCESSING));
        given(messageSource.getMessage(eq("error.order.invalid.state"), any(), any(Locale.class)))
                .willReturn("Invalid state");

        mvc.perform(post("/api/orders/" + ORDER_ID + "/request-post-shipment-cancellation")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Reason\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_ORDER_STATE"));
    }

    @Test
    void requestPostShipmentCancellation_missingIban_returns422() throws Exception {
        given(orderService.requestPostShipmentCancellation(eq(BUYER_EMAIL), eq(ORDER_ID), any(), any(), any()))
                .willThrow(new MissingBuyerIbanException(ORDER_ID));
        given(messageSource.getMessage(eq("error.order.missing.buyer.iban"), any(), any(Locale.class)))
                .willReturn("IBAN required");

        mvc.perform(post("/api/orders/" + ORDER_ID + "/request-post-shipment-cancellation")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Reason\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("MISSING_BUYER_IBAN"));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String checkoutBody(String paymentMethod) {
        return """
                {
                  "addressId": "%s",
                  "carrierId": "%s",
                  "paymentMethod": "%s"
                }""".formatted(UUID.randomUUID(), UUID.randomUUID(), paymentMethod);
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
        setField(r, "buyerId", UUID.randomUUID());
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
