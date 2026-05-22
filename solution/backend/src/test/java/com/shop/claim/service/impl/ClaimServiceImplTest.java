package com.shop.claim.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.DeliveryAddress;
import com.shop.account.repository.AccountRepository;
import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.dto.CreateClaimRequest;
import com.shop.claim.entity.Claim;
import com.shop.claim.entity.ClaimReason;
import com.shop.claim.entity.ClaimStatus;
import com.shop.claim.exception.ClaimAlreadyOpenException;
import com.shop.claim.exception.ClaimNotFoundException;
import com.shop.claim.repository.ClaimRepository;
import com.shop.notification.service.NotificationService;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link ClaimServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock ClaimRepository claimRepository;
    @Mock OrderRepository orderRepository;
    @Mock AccountRepository accountRepository;
    @Mock NotificationService notificationService;

    ClaimServiceImpl service;

    private static final UUID BUYER_ID  = UUID.randomUUID();
    private static final UUID VENDOR_ID = UUID.randomUUID();
    private static final UUID ORDER_ID  = UUID.randomUUID();
    private static final UUID CLAIM_ID  = UUID.randomUUID();
    private static final String BUYER_EMAIL  = "buyer@test.com";
    private static final String VENDOR_EMAIL = "vendor@test.com";

    @BeforeEach
    void setUp() {
        service = new ClaimServiceImpl(claimRepository, orderRepository, accountRepository, notificationService);

        Account buyer = new Account();
        setField(buyer, "id", BUYER_ID);
        buyer.setEmail(BUYER_EMAIL);
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyer));
    }

    // ─── openClaim ───────────────────────────────────────────────────────────

    @Test
    void openClaim_createsClaimInOpenStatus() {
        Order order = buildOrder(OrderStatus.SHIPPED);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));
        given(claimRepository.existsByOrderIdAndBuyerIdAndStatus(ORDER_ID, BUYER_ID, ClaimStatus.OPEN)).willReturn(false);
        Claim saved = buildSavedClaim(ClaimStatus.OPEN);
        given(claimRepository.save(any())).willReturn(saved);

        ClaimResponse result = service.openClaim(BUYER_EMAIL, ORDER_ID, buildRequest(), Locale.FRENCH);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.OPEN);
        then(notificationService).should().sendClaimOpenedEmail(any(), any(), any());
    }

    @Test
    void openClaim_orderNotFound_throws() {
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.openClaim(BUYER_EMAIL, ORDER_ID, buildRequest(), Locale.FRENCH))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void openClaim_cancelledOrder_throwsInvalidOrderState() {
        Order order = buildOrder(OrderStatus.CANCELLED);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.openClaim(BUYER_EMAIL, ORDER_ID, buildRequest(), Locale.FRENCH))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void openClaim_claimAlreadyOpen_throws() {
        Order order = buildOrder(OrderStatus.DELIVERED);
        given(orderRepository.findByIdAndBuyerId(ORDER_ID, BUYER_ID)).willReturn(Optional.of(order));
        given(claimRepository.existsByOrderIdAndBuyerIdAndStatus(ORDER_ID, BUYER_ID, ClaimStatus.OPEN)).willReturn(true);

        assertThatThrownBy(() -> service.openClaim(BUYER_EMAIL, ORDER_ID, buildRequest(), Locale.FRENCH))
                .isInstanceOf(ClaimAlreadyOpenException.class);
    }

    // ─── getMyClaims ─────────────────────────────────────────────────────────

    @Test
    void getMyClaims_returnsEmptyWhenNoneExist() {
        given(claimRepository.findByBuyerIdOrderByCreatedAtDesc(BUYER_ID)).willReturn(List.of());

        assertThat(service.getMyClaims(BUYER_EMAIL)).isEmpty();
    }

    // ─── getMyClaim ──────────────────────────────────────────────────────────

    @Test
    void getMyClaim_throwsWhenNotFound() {
        given(claimRepository.findByIdAndBuyerId(CLAIM_ID, BUYER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyClaim(BUYER_EMAIL, CLAIM_ID))
                .isInstanceOf(ClaimNotFoundException.class);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Order buildOrder(OrderStatus status) {
        Order o = new Order();
        setField(o, "id", ORDER_ID);
        o.setBuyerId(BUYER_ID);
        o.setVendorId(VENDOR_ID);
        o.setVendorEmail(VENDOR_EMAIL);
        o.setOrderNumber("ORD-TEST-001");
        o.setCarrierId(UUID.randomUUID());
        o.setCarrierName("Test Carrier");
        o.setCarrierTrackingUrl("https://track.example.com");
        o.setDeliveryAddress(buildDeliveryAddress());
        o.setPaymentMethod(PaymentMethod.CARD);
        o.setStatus(status);
        o.setTotalAmountTtc(new BigDecimal("24.00"));
        return o;
    }

    private Claim buildSavedClaim(ClaimStatus status) {
        Claim c = new Claim();
        setField(c, "id", CLAIM_ID);
        c.setOrderId(ORDER_ID);
        c.setOrderNumber("ORD-TEST-001");
        c.setBuyerId(BUYER_ID);
        c.setBuyerEmail(BUYER_EMAIL);
        c.setVendorId(VENDOR_ID);
        c.setVendorEmail(VENDOR_EMAIL);
        c.setReason(ClaimReason.NON_RECEIPT);
        c.setMessage("My order never arrived.");
        c.setStatus(status);
        return c;
    }

    private CreateClaimRequest buildRequest() {
        CreateClaimRequest r = new CreateClaimRequest();
        r.setReason(ClaimReason.NON_RECEIPT);
        r.setMessage("My order never arrived.");
        return r;
    }

    private DeliveryAddress buildDeliveryAddress() {
        Account owner = new Account();
        setField(owner, "id", BUYER_ID);
        DeliveryAddress a = new DeliveryAddress();
        setField(a, "id", UUID.randomUUID());
        a.setAccount(owner);
        a.setLabel("Home");
        a.setAddressLine("1 rue Test");
        a.setCity("Paris");
        a.setPostalCode("75001");
        a.setCountryCode("FR");
        return a;
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
