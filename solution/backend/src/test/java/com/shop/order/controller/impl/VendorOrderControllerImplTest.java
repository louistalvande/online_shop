package com.shop.order.controller.impl;

import com.shop.common.GlobalExceptionHandler;
import com.shop.order.dto.OrderResponse;
import com.shop.order.entity.OrderStatus;
import com.shop.order.entity.PaymentMethod;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.service.VendorOrderService;
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

/** Unit tests for {@link VendorOrderControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class VendorOrderControllerImplTest {

    @Mock VendorOrderService vendorOrderService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final UUID VENDOR_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();

    private final UsernamePasswordAuthenticationToken vendorPrincipal =
            new UsernamePasswordAuthenticationToken(VENDOR_ID.toString(), null, List.of());

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new VendorOrderControllerImpl(vendorOrderService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    @Test
    void listOrders_returns200WithOrders() throws Exception {
        given(vendorOrderService.getVendorOrders(VENDOR_ID))
                .willReturn(List.of(buildOrderResponse(OrderStatus.AWAITING_PROCESSING)));

        mvc.perform(get("/api/vendor/orders").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-VND-TEST"));
    }

    @Test
    void getOrder_found_returns200() throws Exception {
        given(vendorOrderService.getVendorOrder(VENDOR_ID, ORDER_ID))
                .willReturn(buildOrderResponse(OrderStatus.PAYMENT_PENDING_WIRE));

        mvc.perform(get("/api/vendor/orders/" + ORDER_ID).principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()));
    }

    @Test
    void getOrder_notFound_returns404() throws Exception {
        given(vendorOrderService.getVendorOrder(VENDOR_ID, ORDER_ID))
                .willThrow(new OrderNotFoundException(ORDER_ID));
        given(messageSource.getMessage(eq("error.order.not.found"), any(), any(Locale.class)))
                .willReturn("Order not found");

        mvc.perform(get("/api/vendor/orders/" + ORDER_ID).principal(vendorPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
    }

    @Test
    void confirmWire_returns200WithUpdatedOrder() throws Exception {
        given(vendorOrderService.confirmWirePayment(eq(VENDOR_ID), eq(ORDER_ID), any()))
                .willReturn(buildOrderResponse(OrderStatus.AWAITING_PROCESSING));

        mvc.perform(post("/api/vendor/orders/" + ORDER_ID + "/confirm-wire").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AWAITING_PROCESSING"));
    }

    @Test
    void confirmWire_invalidState_returns409() throws Exception {
        given(vendorOrderService.confirmWirePayment(eq(VENDOR_ID), eq(ORDER_ID), any()))
                .willThrow(new InvalidOrderStateException(ORDER_ID, OrderStatus.AWAITING_PROCESSING));
        given(messageSource.getMessage(eq("error.order.invalid.state"), any(), any(Locale.class)))
                .willReturn("Invalid state");

        mvc.perform(post("/api/vendor/orders/" + ORDER_ID + "/confirm-wire").principal(vendorPrincipal))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectWire_returns200WithCancelledOrder() throws Exception {
        given(vendorOrderService.rejectWirePayment(eq(VENDOR_ID), eq(ORDER_ID), any()))
                .willReturn(buildOrderResponse(OrderStatus.CANCELLED));

        mvc.perform(post("/api/vendor/orders/" + ORDER_ID + "/reject-wire").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void ship_returns200WithShippedOrder() throws Exception {
        OrderResponse shipped = buildOrderResponse(OrderStatus.SHIPPED);
        setField(shipped, "trackingNumber", "TRACK123");
        given(vendorOrderService.shipOrder(eq(VENDOR_ID), eq(ORDER_ID), eq("TRACK123"), any()))
                .willReturn(shipped);

        mvc.perform(post("/api/vendor/orders/" + ORDER_ID + "/ship")
                        .principal(vendorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingNumber\":\"TRACK123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.trackingNumber").value("TRACK123"));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

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
        setField(r, "orderNumber", "ORD-VND-TEST");
        setField(r, "buyerId", UUID.randomUUID());
        setField(r, "carrierId", UUID.randomUUID());
        setField(r, "carrierName", "Test Carrier");
        setField(r, "carrierTrackingUrl", "https://track.example.com");
        setField(r, "deliveryAddressLine", "1 rue Test");
        setField(r, "deliveryCity", "Paris");
        setField(r, "deliveryPostalCode", "75001");
        setField(r, "deliveryCountryCode", "FR");
        setField(r, "paymentMethod", PaymentMethod.WIRE_TRANSFER);
        setField(r, "status", status);
        setField(r, "totalAmountTtc", new BigDecimal("20.00"));
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
