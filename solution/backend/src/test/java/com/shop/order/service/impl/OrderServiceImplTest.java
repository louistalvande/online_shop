package com.shop.order.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.DeliveryAddress;
import com.shop.account.repository.AccountRepository;
import com.shop.account.repository.DeliveryAddressRepository;
import com.shop.cart.entity.Cart;
import com.shop.cart.entity.CartItem;
import com.shop.cart.repository.CartRepository;
import com.shop.carrier.entity.Carrier;
import com.shop.carrier.repository.CarrierRepository;
import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.repository.ProductRepository;
import com.shop.country.repository.CountryRepository;
import com.shop.notification.service.NotificationService;
import com.shop.order.dto.CheckoutInitResponse;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.dto.OrderResponse;
import com.shop.order.entity.Order;
import com.shop.order.entity.OrderStatus;
import com.shop.order.entity.PaymentMethod;
import com.shop.order.exception.CarrierNotAvailableException;
import com.shop.order.exception.EmptyCartException;
import com.shop.order.exception.InvalidDeliveryCountryException;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.MissingBuyerIbanException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.exception.PaymentFailedException;
import com.shop.order.repository.OrderRepository;
import com.shop.payment.PaymentGateway;
import com.shop.payment.PaymentIntentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link OrderServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock CarrierRepository carrierRepository;
    @Mock CountryRepository countryRepository;
    @Mock AccountRepository accountRepository;
    @Mock DeliveryAddressRepository deliveryAddressRepository;
    @Mock ProductRepository productRepository;
    @Mock PaymentGateway paymentGateway;
    @Mock NotificationService notificationService;

    OrderServiceImpl service;

    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final String BUYER_EMAIL = "buyer@test.com";
    private static final UUID CARRIER_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID ADDRESS_ID = UUID.randomUUID();
    private static final String IBAN = "FR7630006000011234567890189";
    private static final String BIC = "BNPAFRPPXXX";

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                orderRepository, cartRepository, carrierRepository,
                countryRepository, accountRepository, deliveryAddressRepository,
                productRepository, paymentGateway, notificationService, IBAN, BIC);

        Account buyerAccount = new Account();
        setField(buyerAccount, "id", BUYER_ID);
        buyerAccount.setEmail(BUYER_EMAIL);
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyerAccount));
    }

    // ─── initCheckout ───────────────────────────────────────────────────────

    @Test
    void initCheckout_card_createsOrderAndReturnsClientSecret() {
        Cart cart = buildCart();
        given(cartRepository.findByBuyerId(BUYER_ID)).willReturn(Optional.of(cart));
        given(deliveryAddressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, BUYER_ID))
                .willReturn(Optional.of(buildDeliveryAddress("FR")));
        given(countryRepository.existsByCode("FR")).willReturn(true);
        given(carrierRepository.findById(CARRIER_ID)).willReturn(Optional.of(buildCarrier("FR")));
        given(paymentGateway.createPaymentIntent(any(), any()))
                .willReturn(new PaymentIntentResult("pi_test", "pi_test_secret"));
        Order saved = buildSavedOrder(OrderStatus.PAYMENT_PENDING_CARD);
        given(orderRepository.save(any())).willReturn(saved);

        CheckoutInitResponse response = service.initCheckout(BUYER_EMAIL, cardRequest(), Locale.FRENCH);

        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(response.getClientSecret()).isNotNull();
        then(cartRepository).should().delete(cart);
    }

    @Test
    void initCheckout_wire_setsStatusAndSendsEmails() {
        Cart cart = buildCart();
        given(cartRepository.findByBuyerId(BUYER_ID)).willReturn(Optional.of(cart));
        given(deliveryAddressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, BUYER_ID))
                .willReturn(Optional.of(buildDeliveryAddress("FR")));
        given(countryRepository.existsByCode("FR")).willReturn(true);
        given(carrierRepository.findById(CARRIER_ID)).willReturn(Optional.of(buildCarrier("FR")));
        Order saved = buildSavedOrder(OrderStatus.PAYMENT_PENDING_WIRE);
        given(orderRepository.save(any())).willReturn(saved);
        given(accountRepository.findAllByRole(AccountRole.VENDOR)).willReturn(List.of(buildVendorAccount()));

        CheckoutInitResponse response = service.initCheckout(BUYER_EMAIL, wireRequest(), Locale.FRENCH);

        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.WIRE_TRANSFER);
        assertThat(response.getBankIban()).isEqualTo(IBAN);
        then(notificationService).should().sendWireTransferDetailsEmail(any(), any(), any(), any(), any(), any());
        then(notificationService).should().sendVendorNewOrderEmail(any(), any(), any());
    }

    @Test
    void initCheckout_emptyCart_throws() {
        given(cartRepository.findByBuyerId(BUYER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.initCheckout(BUYER_EMAIL, cardRequest(), Locale.FRENCH))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void initCheckout_invalidCountry_throws() {
        given(cartRepository.findByBuyerId(BUYER_ID)).willReturn(Optional.of(buildCart()));
        given(deliveryAddressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, BUYER_ID))
                .willReturn(Optional.of(buildDeliveryAddress("US")));
        given(countryRepository.existsByCode("US")).willReturn(false);

        assertThatThrownBy(() -> service.initCheckout(BUYER_EMAIL, cardRequest(), Locale.FRENCH))
                .isInstanceOf(InvalidDeliveryCountryException.class);
    }

    @Test
    void initCheckout_carrierNotAvailableForCountry_throws() {
        given(cartRepository.findByBuyerId(BUYER_ID)).willReturn(Optional.of(buildCart()));
        given(deliveryAddressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, BUYER_ID))
                .willReturn(Optional.of(buildDeliveryAddress("FR")));
        given(countryRepository.existsByCode("FR")).willReturn(true);
        given(carrierRepository.findById(CARRIER_ID)).willReturn(Optional.of(buildCarrier("DE")));

        assertThatThrownBy(() -> service.initCheckout(BUYER_EMAIL, cardRequest(), Locale.FRENCH))
                .isInstanceOf(CarrierNotAvailableException.class);
    }

    // ─── confirmCardPayment ──────────────────────────────────────────────────

    @Test
    void confirmCardPayment_transitionsToAwaitingProcessing() {
        Order order = buildSavedOrder(OrderStatus.PAYMENT_PENDING_CARD);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));
        given(paymentGateway.isPaymentSucceeded(any())).willReturn(true);
        given(orderRepository.save(any())).willReturn(order);
        given(accountRepository.findAllByRole(AccountRole.VENDOR)).willReturn(List.of(buildVendorAccount()));

        service.confirmCardPayment(BUYER_EMAIL, ORDER_ID, Locale.FRENCH);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.AWAITING_PROCESSING);
        then(notificationService).should().sendOrderConfirmationEmail(any(), any(), any());
        then(notificationService).should().sendVendorNewOrderEmail(any(), any(), any());
    }

    @Test
    void confirmCardPayment_wrongStatus_throws() {
        Order order = buildSavedOrder(OrderStatus.AWAITING_PROCESSING);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.confirmCardPayment(BUYER_EMAIL, ORDER_ID, Locale.FRENCH))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void confirmCardPayment_paymentNotSucceeded_throws() {
        Order order = buildSavedOrder(OrderStatus.PAYMENT_PENDING_CARD);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));
        given(paymentGateway.isPaymentSucceeded(any())).willReturn(false);

        assertThatThrownBy(() -> service.confirmCardPayment(BUYER_EMAIL, ORDER_ID, Locale.FRENCH))
                .isInstanceOf(PaymentFailedException.class);
    }

    @Test
    void confirmCardPayment_orderNotFound_throws() {
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmCardPayment(BUYER_EMAIL, ORDER_ID, Locale.FRENCH))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ─── cancelOrder ────────────────────────────────────────────────────────

    @Test
    void cancelOrder_card_transitionsToCancelled() {
        Order order = buildSavedOrder(OrderStatus.AWAITING_PROCESSING);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));
        given(orderRepository.save(any())).willReturn(order);

        service.cancelOrder(BUYER_EMAIL, ORDER_ID, null, Locale.FRENCH);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        then(paymentGateway).should().refund("pi_stub");
        then(notificationService).should().sendBuyerCancellationEmail(any(), any(), any());
    }

    @Test
    void cancelOrder_wire_transitionsToWireRefundInProgress() {
        Order order = buildSavedOrder(OrderStatus.AWAITING_PROCESSING);
        order.setPaymentMethod(PaymentMethod.WIRE_TRANSFER);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));
        given(orderRepository.save(any())).willReturn(order);

        service.cancelOrder(BUYER_EMAIL, ORDER_ID, "FR7630006000011234567890189", Locale.FRENCH);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.WIRE_REFUND_IN_PROGRESS);
        assertThat(order.getBuyerIban()).isEqualTo("FR7630006000011234567890189");
    }

    @Test
    void cancelOrder_wire_missingIban_throwsMissingBuyerIbanException() {
        Order order = buildSavedOrder(OrderStatus.AWAITING_PROCESSING);
        order.setPaymentMethod(PaymentMethod.WIRE_TRANSFER);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelOrder(BUYER_EMAIL, ORDER_ID, null, Locale.FRENCH))
                .isInstanceOf(MissingBuyerIbanException.class);
    }

    @Test
    void cancelOrder_wire_paymentPending_transitionsToWireRefundInProgress() {
        Order order = buildSavedOrder(OrderStatus.PAYMENT_PENDING_WIRE);
        order.setPaymentMethod(PaymentMethod.WIRE_TRANSFER);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));
        given(orderRepository.save(any())).willReturn(order);

        service.cancelOrder(BUYER_EMAIL, ORDER_ID, "FR7630006000011234567890189", Locale.FRENCH);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.WIRE_REFUND_IN_PROGRESS);
        assertThat(order.getBuyerIban()).isEqualTo("FR7630006000011234567890189");
    }

    @Test
    void cancelOrder_wrongState_throwsInvalidOrderStateException() {
        Order order = buildSavedOrder(OrderStatus.SHIPPED);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelOrder(BUYER_EMAIL, ORDER_ID, null, Locale.FRENCH))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    // ─── requestPostShipmentCancellation ────────────────────────────────────

    @Test
    void requestPostShipmentCancellation_card_transitionsToCancellationRequested() {
        Order order = buildSavedOrder(OrderStatus.SHIPPED);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));
        given(orderRepository.save(any())).willReturn(order);

        service.requestPostShipmentCancellation(BUYER_EMAIL, ORDER_ID, "Product defective", null, Locale.FRENCH);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLATION_REQUESTED_AFTER_SHIPMENT);
        assertThat(order.getCancellationReason()).isEqualTo("Product defective");
    }

    @Test
    void requestPostShipmentCancellation_wire_storesIban() {
        Order order = buildSavedOrder(OrderStatus.SHIPPED);
        order.setPaymentMethod(PaymentMethod.WIRE_TRANSFER);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));
        given(orderRepository.save(any())).willReturn(order);

        service.requestPostShipmentCancellation(BUYER_EMAIL, ORDER_ID, "Changed mind", "FR7630006000011234567890189", Locale.FRENCH);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLATION_REQUESTED_AFTER_SHIPMENT);
        assertThat(order.getBuyerIban()).isEqualTo("FR7630006000011234567890189");
    }

    @Test
    void requestPostShipmentCancellation_wire_missingIban_throwsMissingBuyerIbanException() {
        Order order = buildSavedOrder(OrderStatus.SHIPPED);
        order.setPaymentMethod(PaymentMethod.WIRE_TRANSFER);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.requestPostShipmentCancellation(BUYER_EMAIL, ORDER_ID, "reason", null, Locale.FRENCH))
                .isInstanceOf(MissingBuyerIbanException.class);
    }

    @Test
    void requestPostShipmentCancellation_wrongState_throwsInvalidOrderStateException() {
        Order order = buildSavedOrder(OrderStatus.AWAITING_PROCESSING);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.requestPostShipmentCancellation(BUYER_EMAIL, ORDER_ID, "reason", null, Locale.FRENCH))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    // ─── getMyOrders / getMyOrder ────────────────────────────────────────────

    @Test
    void getMyOrders_returnsEmptyListWhenNoneExist() {
        given(orderRepository.findByBuyerIdOrderByCreatedAtDesc(BUYER_ID)).willReturn(List.of());

        List<OrderResponse> result = service.getMyOrders(BUYER_EMAIL);

        assertThat(result).isEmpty();
    }

    @Test
    void getMyOrder_throwsWhenNotFound() {
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyOrder(BUYER_EMAIL, ORDER_ID))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Cart buildCart() {
        Cart c = new Cart();
        setField(c, "id", UUID.randomUUID());
        setField(c, "items", new ArrayList<>(List.of(buildCartItem())));
        c.setBuyerId(BUYER_ID);
        return c;
    }

    private CartItem buildCartItem() {
        CartItem item = new CartItem();
        setField(item, "id", UUID.randomUUID());
        item.setProduct(buildProduct());
        item.setQuantity(2);
        return item;
    }

    private Product buildProduct() {
        Product p = new Product();
        setField(p, "id", UUID.randomUUID());
        p.setName("Test product");
        p.setPriceExclTax(new BigDecimal("10.00"));
        p.setQuantity(10);
        p.setStatus(ProductStatus.PUBLISHED);
        return p;
    }

    private Carrier buildCarrier(String... countries) {
        Carrier c = new Carrier();
        setField(c, "id", CARRIER_ID);
        c.setName("Test Carrier");
        c.setTrackingUrl("https://track.example.com");
        c.setActive(true);
        c.setSupportedCountries(new ArrayList<>(List.of(countries)));
        return c;
    }

    private DeliveryAddress buildDeliveryAddress(String countryCode) {
        Account owner = new Account();
        setField(owner, "id", BUYER_ID);
        DeliveryAddress a = new DeliveryAddress();
        setField(a, "id", ADDRESS_ID);
        a.setAccount(owner);
        a.setLabel("Home");
        a.setAddressLine("1 rue Test");
        a.setCity("Paris");
        a.setPostalCode("75001");
        a.setCountryCode(countryCode);
        a.setDefault(true);
        return a;
    }

    private Order buildSavedOrder(OrderStatus status) {
        Order o = new Order();
        setField(o, "id", ORDER_ID);
        setField(o, "lines", new ArrayList<>());
        o.setOrderNumber("ORD-20260518-TEST001");
        o.setBuyerId(BUYER_ID);
        o.setCarrierId(CARRIER_ID);
        o.setCarrierName("Test Carrier");
        o.setCarrierTrackingUrl("https://track.example.com");
        o.setDeliveryAddress(buildDeliveryAddress("FR"));
        o.setPaymentMethod(PaymentMethod.CARD);
        o.setStatus(status);
        o.setTotalAmountTtc(new BigDecimal("24.00"));
        o.setStripePaymentIntentId("pi_stub");
        return o;
    }

    private CreateOrderRequest cardRequest() {
        CreateOrderRequest r = new CreateOrderRequest();
        r.setAddressId(ADDRESS_ID);
        r.setCarrierId(CARRIER_ID);
        r.setPaymentMethod(PaymentMethod.CARD);
        return r;
    }

    private CreateOrderRequest wireRequest() {
        CreateOrderRequest r = cardRequest();
        r.setPaymentMethod(PaymentMethod.WIRE_TRANSFER);
        return r;
    }

    private Account buildVendorAccount() {
        Account v = new Account();
        v.setEmail("vendor@test.com");
        v.setRole(AccountRole.VENDOR);
        return v;
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
