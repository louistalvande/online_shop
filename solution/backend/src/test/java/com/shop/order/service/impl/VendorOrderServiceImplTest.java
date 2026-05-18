package com.shop.order.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.repository.AccountRepository;
import com.shop.catalog.repository.ProductRepository;
import com.shop.notification.service.NotificationService;
import com.shop.order.dto.OrderResponse;
import com.shop.order.entity.Order;
import com.shop.order.entity.OrderStatus;
import com.shop.order.entity.PaymentMethod;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link VendorOrderServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class VendorOrderServiceImplTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock AccountRepository accountRepository;
    @Mock NotificationService notificationService;

    VendorOrderServiceImpl service;

    private static final UUID VENDOR_ID = UUID.randomUUID();
    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new VendorOrderServiceImpl(orderRepository, productRepository, accountRepository, notificationService);
    }

    @Test
    void getVendorOrders_returnsAllVendorOrders() {
        given(orderRepository.findByVendorIdOrderByCreatedAtDesc(VENDOR_ID))
                .willReturn(List.of(buildOrder(OrderStatus.AWAITING_PROCESSING)));

        List<OrderResponse> result = service.getVendorOrders(VENDOR_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo("ORD-VND-TEST");
    }

    @Test
    void getVendorOrder_found_returnsResponse() {
        given(orderRepository.findByIdAndVendorId(ORDER_ID, VENDOR_ID))
                .willReturn(Optional.of(buildOrder(OrderStatus.AWAITING_PROCESSING)));

        OrderResponse result = service.getVendorOrder(VENDOR_ID, ORDER_ID);

        assertThat(result.getId()).isEqualTo(ORDER_ID);
    }

    @Test
    void getVendorOrder_notFound_throwsOrderNotFoundException() {
        given(orderRepository.findByIdAndVendorId(ORDER_ID, VENDOR_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVendorOrder(VENDOR_ID, ORDER_ID))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void confirmWirePayment_transitionsToAwaitingProcessing() {
        Order order = buildOrder(OrderStatus.PAYMENT_PENDING_WIRE);
        given(orderRepository.findByIdAndVendorId(ORDER_ID, VENDOR_ID)).willReturn(Optional.of(order));
        given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        Account buyer = new Account();
        buyer.setEmail("buyer@example.com");
        given(accountRepository.findById(BUYER_ID)).willReturn(Optional.of(buyer));

        OrderResponse result = service.confirmWirePayment(VENDOR_ID, ORDER_ID, Locale.FRENCH);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.AWAITING_PROCESSING);
        then(notificationService).should().sendOrderConfirmationEmail(any(), any(), any());
    }

    @Test
    void confirmWirePayment_wrongState_throwsInvalidOrderStateException() {
        Order order = buildOrder(OrderStatus.AWAITING_PROCESSING);
        given(orderRepository.findByIdAndVendorId(ORDER_ID, VENDOR_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.confirmWirePayment(VENDOR_ID, ORDER_ID, Locale.FRENCH))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void rejectWirePayment_transitionsToCancelled() {
        Order order = buildOrder(OrderStatus.PAYMENT_PENDING_WIRE);
        given(orderRepository.findByIdAndVendorId(ORDER_ID, VENDOR_ID)).willReturn(Optional.of(order));
        given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        Account buyer = new Account();
        buyer.setEmail("buyer@example.com");
        given(accountRepository.findById(BUYER_ID)).willReturn(Optional.of(buyer));

        OrderResponse result = service.rejectWirePayment(VENDOR_ID, ORDER_ID, Locale.FRENCH);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        then(notificationService).should().sendWirePaymentRejectedEmail(any(), any(), any());
    }

    @Test
    void rejectWirePayment_wrongState_throwsInvalidOrderStateException() {
        Order order = buildOrder(OrderStatus.SHIPPED);
        given(orderRepository.findByIdAndVendorId(ORDER_ID, VENDOR_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.rejectWirePayment(VENDOR_ID, ORDER_ID, Locale.FRENCH))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void rejectWirePayment_notFound_throwsOrderNotFoundException() {
        given(orderRepository.findByIdAndVendorId(ORDER_ID, VENDOR_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.rejectWirePayment(VENDOR_ID, ORDER_ID, Locale.FRENCH))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Order buildOrder(OrderStatus status) {
        Order o = new Order();
        setField(o, "id", ORDER_ID);
        setField(o, "orderNumber", "ORD-VND-TEST");
        setField(o, "buyerId", BUYER_ID);
        setField(o, "vendorId", VENDOR_ID);
        setField(o, "vendorEmail", "vendor@example.com");
        setField(o, "carrierId", UUID.randomUUID());
        setField(o, "carrierName", "Test Carrier");
        setField(o, "carrierTrackingUrl", "https://track.example.com");
        setField(o, "deliveryAddressLine", "1 rue Test");
        setField(o, "deliveryCity", "Paris");
        setField(o, "deliveryPostalCode", "75001");
        setField(o, "deliveryCountryCode", "FR");
        setField(o, "paymentMethod", PaymentMethod.WIRE_TRANSFER);
        setField(o, "status", status);
        setField(o, "totalAmountTtc", new BigDecimal("24.00"));
        setField(o, "lines", new ArrayList<>());
        setField(o, "createdAt", LocalDateTime.now());
        setField(o, "updatedAt", LocalDateTime.now());
        return o;
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
